package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.AdminUserRequest;
import fr.CDA.GHE.dto.ClubAffiliationRequest;
import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.CreateUserRequest;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.UpdateUserRequest;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.ClubMapper;
import fr.CDA.GHE.mapper.UserMapper;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.util.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Gère le cycle de vie des comptes utilisateurs.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // Format simplifié, suffisant pour un contrôle serveur (CU5, règle métier n°3).
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    // Alphabet du mot de passe temporaire : exclut les caractères ambigus (I/O/0/1...).
    private static final String PWD_UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String PWD_LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String PWD_DIGITS = "23456789";
    private static final String PWD_SPECIAL = "!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final UserMapper userMapper;
    private final ClubMapper clubMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String baseUrl;

    public UserService(UserRepository userRepository, ClubRepository clubRepository, UserMapper userMapper,
                        ClubMapper clubMapper, PasswordEncoder passwordEncoder, EmailService emailService,
                        @Value("${app.base-url}") String baseUrl) {
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.userMapper = userMapper;
        this.clubMapper = clubMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.baseUrl = baseUrl;
    }

    /**
     * Crée un compte membre non affilié (CU5, SFG §2.8).
     * <p>
     * Statut {@link AccountStatus#INACTIVE} et rôle {@link Role#MEMBER} sont forcés ici :
     * jamais choisis par le client (règle métier n°9 : toujours non affilié à un club — pas
     * de relation club sur ce chemin de création). Le mot de passe est hashé, jamais stocké
     * en clair.
     *
     * @param request informations saisies par le visiteur
     * @return le compte créé
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @Transactional
    public UserDto createUser(CreateUserRequest request) throws FunctionalException {
        validate(request);

        User user = new User();
        user.setLastName(request.lastName());
        user.setFirstName(request.firstName());
        user.setPostalAddress(request.postalAddress());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(AccountStatus.INACTIVE);
        user.setRole(Role.MEMBER);
        user.setActivationToken(UUID.randomUUID().toString());

        User created = userRepository.save(user);
        log.info("CREATION compte non affilié : id={} email={}", created.getId(), created.getEmail());

        // CU5 règle métier n°10 / CU6 : email d'activation (lien à usage unique).
        String activationLink = baseUrl + "/auth/activate?token=" + created.getActivationToken();
        emailService.sendActivationEmail(created.getEmail(), activationLink);

        return userMapper.toDto(created);
    }

    /**
     * Active un compte à partir du lien reçu par email (CU6, SFG §2.9).
     *
     * @param token jeton d'activation transmis dans le lien
     * @return le compte activé
     * @throws FunctionalException si le lien est invalide ou si le compte ne peut plus être activé
     */
    @Transactional
    public UserDto activateAccount(String token) throws FunctionalException {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new FunctionalException("Le compte associé à ce lien n'existe pas."));

        if (user.getStatus() != AccountStatus.INACTIVE) {
            throw new FunctionalException("Ce compte est déjà actif ou ne peut plus être activé.");
        }

        user.setStatus(AccountStatus.ACTIVE);
        user.setActivationToken(null); // jeton à usage unique

        log.info("ACTIVATION compte : id={} email={}", user.getId(), user.getEmail());

        // Entité gérée : le dirty checking JPA persiste les changements au commit.
        return userMapper.toDto(user);
    }

    /**
     * Modifie le compte de l'utilisateur connecté (CU13, SFG §2.16).
     * <p>
     * Contrairement à la création (CU5), où tout champ est obligatoirement saisi et validé,
     * une règle n'est ici revérifiée que si le champ concerné change réellement :
     * <ul>
     *     <li>email : vérifié unique seulement si différent de l'email actuel (règle n°3),
     *     en excluant le compte courant de la recherche (sinon il se bloquerait lui-même) ;</li>
     *     <li>mot de passe : laissé vide, il n'est pas modifié ; renseigné, il doit être fort
     *     (règle n°4).</li>
     * </ul>
     * Les autres champs (nom, prénom, adresse, email, téléphone) sont appliqués immédiatement.
     * Le mot de passe, lui, ne l'est <strong>pas</strong> : conformément au CdC (« Si je ne
     * confirme pas en cliquant sur le lien, mon mot de passe n'est pas modifié »), le nouveau
     * mot de passe (déjà hashé) est mis en attente et un email de confirmation est envoyé ;
     * l'ancien mot de passe reste actif tant que le lien n'a pas été cliqué (voir
     * {@link #confirmPasswordChange(String)}).
     * Les clubs affiliés ne sont pas modifiables ici (champ désactivé côté maquette CU13).
     *
     * @param request nouvelles informations
     * @return le compte modifié
     * @throws FunctionalException si le compte n'est pas actif, ou si une règle métier n'est pas respectée
     */
    @Transactional
    public UserDto updateOwnAccount(UpdateUserRequest request) throws FunctionalException {
        User user = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new FunctionalException("Votre compte doit être actif pour modifier vos informations.");
        }

        if (isBlank(request.lastName()) || isBlank(request.firstName())
                || isBlank(request.postalAddress()) || isBlank(request.email())) {
            throw new FunctionalException("Veuillez renseigner tous les champs obligatoires.");
        }

        boolean emailChanged = !request.email().equalsIgnoreCase(user.getEmail());
        if (emailChanged && userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
            throw new FunctionalException("Cette adresse email est déjà utilisée.");
        }

        boolean passwordChanged = !isBlank(request.password());
        if (passwordChanged && !isStrongPassword(request.password())) {
            throw new FunctionalException("Le mot de passe ne respecte pas les critères de sécurité requis.");
        }

        user.setLastName(request.lastName());
        user.setFirstName(request.firstName());
        user.setPostalAddress(request.postalAddress());
        user.setEmail(request.email());
        user.setPhone(request.phone());

        if (passwordChanged) {
            user.setPendingPassword(passwordEncoder.encode(request.password()));
            user.setPasswordChangeToken(UUID.randomUUID().toString());
        }

        log.info("MODIFICATION compte (self-service) : id={} email={}", user.getId(), user.getEmail());

        if (passwordChanged) {
            String confirmationLink = baseUrl + "/auth/confirm-password-change?token=" + user.getPasswordChangeToken();
            emailService.sendPasswordChangeConfirmationEmail(user.getEmail(), confirmationLink);
        }

        // Entité gérée : le dirty checking JPA persiste les changements au commit.
        return userMapper.toDto(user);
    }

    /**
     * Confirme un changement de mot de passe à partir du lien reçu par email (CU13).
     * <p>
     * Applique le mot de passe mis en attente par {@link #updateOwnAccount(UpdateUserRequest)}
     * et invalide le jeton (lien à usage unique). Tant que ce lien n'a pas été cliqué,
     * l'ancien mot de passe reste seul valide pour se connecter.
     *
     * @param token jeton de confirmation transmis dans le lien
     * @return le compte dont le mot de passe a été confirmé
     * @throws FunctionalException si le lien est invalide
     */
    @Transactional
    public UserDto confirmPasswordChange(String token) throws FunctionalException {
        User user = userRepository.findByPasswordChangeToken(token)
                .orElseThrow(() -> new FunctionalException("Le lien de confirmation est invalide."));

        user.setPassword(user.getPendingPassword());
        user.setPendingPassword(null);
        user.setPasswordChangeToken(null); // jeton à usage unique

        log.info("CONFIRMATION changement de mot de passe : id={} email={}", user.getId(), user.getEmail());

        return userMapper.toDto(user);
    }

    /**
     * Retourne une page d'utilisateurs (CU25 — liste des comptes).
     *
     * @param pageable pagination demandée
     * @return la page d'utilisateurs correspondante
     */
    @Transactional(readOnly = true)
    public PageDto<UserDto> extractAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        List<UserDto> content = userMapper.toDtoList(page.getContent());
        return new PageDto<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }

    /**
     * Retourne un utilisateur par son identifiant.
     *
     * @param id identifiant de l'utilisateur
     * @return l'utilisateur trouvé
     * @throws NotFoundException si aucun utilisateur ne correspond
     */
    @Transactional(readOnly = true)
    public UserDto extractById(Long id) {
        return userMapper.toDto(findUserOrThrow(id));
    }

    /**
     * Crée un compte (membre affilié, organisateur ou administrateur) — CU25, SFG §2.28.
     * <p>
     * Statut {@link AccountStatus#INACTIVE} par défaut, comme pour le signup self-service.
     * Un mot de passe temporaire est généré par le système (jamais choisi par l'admin) et
     * envoyé par email (règle métier de l'action « Bouton Valider »).
     * <p>
     * TODO CPT-06 (à créer) : endpoint de confirmation permettant au titulaire du compte de
     * saisir son mot de passe temporaire puis un mot de passe définitif, faisant passer le
     * compte à ACTIF (CdC §Validation de la création d'un compte). Tant que cet endpoint
     * n'existe pas, un compte créé ici reste INACTIF.
     *
     * @param request informations saisies par l'administrateur
     * @return le compte créé
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @Transactional
    public UserDto createUserByAdmin(AdminUserRequest request) throws FunctionalException {
        validateAdminRequest(request);

        if (userRepository.existsByEmail(request.email())) {
            throw new FunctionalException("Cette adresse email est déjà utilisée.");
        }

        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setLastName(request.lastName());
        user.setFirstName(request.firstName());
        user.setPostalAddress(request.postalAddress());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setStatus(AccountStatus.INACTIVE);
        user.setRole(request.role());

        User created = userRepository.save(user);
        assignClubs(created, request.clubIds());

        log.info("CREATION compte (admin) : id={} email={} role={}",
                created.getId(), created.getEmail(), created.getRole());

        emailService.sendAdminCreatedAccountEmail(created.getEmail(), temporaryPassword);

        return userMapper.toDto(created);
    }

    /**
     * Modifie un compte existant (CU25, SFG §2.28).
     * <p>
     * Règle métier n°3 : l'email modifié ne doit pas entrer en collision avec celui d'un
     * <strong>autre</strong> compte (le compte modifié garde le droit de conserver le sien).
     *
     * @param id      identifiant du compte à modifier
     * @param request nouvelles informations
     * @return le compte modifié
     * @throws NotFoundException   si aucun compte ne correspond à l'identifiant
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @Transactional
    public UserDto updateUserByAdmin(Long id, AdminUserRequest request) throws FunctionalException {
        User user = findUserOrThrow(id);

        validateAdminRequest(request);

        boolean emailChanged = !request.email().equalsIgnoreCase(user.getEmail());
        if (emailChanged && userRepository.existsByEmail(request.email())) {
            throw new FunctionalException("Cette adresse email est déjà utilisée.");
        }

        user.setLastName(request.lastName());
        user.setFirstName(request.firstName());
        user.setPostalAddress(request.postalAddress());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());

        syncClubs(user, request.clubIds());

        log.info("MODIFICATION compte (admin) : id={} email={}", user.getId(), user.getEmail());

        // Entité gérée : le dirty checking JPA persiste les changements au commit.
        return userMapper.toDto(user);
    }

    /**
     * Gère les affiliations club d'un membre ou d'un organisateur (CU26 SFG §2.29, CU27 SFG §2.30).
     * <p>
     * Remplace l'ensemble des clubs auxquels l'utilisateur est rattaché par la liste transmise :
     * ajoute les nouveaux, retire ceux qui n'y figurent plus. Une liste vide est valide — pas une
     * erreur.
     * <p>
     * Conséquence sur le rôle si plus aucun club ne reste (règle métier n°5 des deux CU) :
     * un {@link Role#MEMBER} reste membre, simplement non affilié ; un {@link Role#ORGANIZER}
     * est rétrogradé en {@link Role#MEMBER} non affilié — il ne peut plus organiser d'événement
     * sans être rattaché à un club (règle PO).
     * <p>
     * Ne s'applique pas à un {@link Role#ADMIN} : un administrateur n'a pas d'affiliation club.
     *
     * @param id      identifiant de l'utilisateur (membre ou organisateur)
     * @param request nouvelle liste de clubs
     * @return les clubs auxquels l'utilisateur est désormais affilié
     * @throws NotFoundException   si aucun utilisateur ne correspond à l'identifiant
     * @throws FunctionalException si l'utilisateur est un administrateur, ou si un club n'existe pas
     */
    @Transactional
    public List<ClubDto> updateMemberAffiliations(Long id, ClubAffiliationRequest request) throws FunctionalException {
        User user = findUserOrThrow(id);

        if (user.getRole() == Role.ADMIN) {
            throw new FunctionalException("Cette action ne s'applique pas aux administrateurs.");
        }

        syncClubs(user, request.clubIds());

        List<Club> remainingClubs = clubRepository.findByMembers_Id(user.getId());

        if (user.getRole() == Role.ORGANIZER && remainingClubs.isEmpty()) {
            user.setRole(Role.MEMBER);
            log.info("RETROGRADATION organisateur -> membre non affilié (plus aucun club) : userId={}", user.getId());
        }

        log.info("MODIFICATION affiliations (admin) : userId={} clubIds={}", user.getId(), request.clubIds());

        return clubMapper.toDtoList(remainingClubs);
    }

    /**
     * Supprime un compte (CU25, SFG §2.28).
     * <p>
     * Retire d'abord le compte de ses clubs d'affiliation : sinon la contrainte de clé
     * étrangère de la table {@code affiliation} empêcherait la suppression.
     *
     * @param id identifiant du compte à supprimer
     * @throws NotFoundException si aucun compte ne correspond à l'identifiant
     */
    @Transactional
    public void deleteUserByAdmin(Long id) {
        User user = findUserOrThrow(id);

        clubRepository.findByMembers_Id(id).forEach(club -> club.getMembers().remove(user));
        userRepository.delete(user);

        log.info("SUPPRESSION compte (admin) : id={} email={}", id, user.getEmail());
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
    }

    /**
     * Contrôles communs à la création et à la modification par un administrateur.
     * <p>
     * L'unicité de l'email est vérifiée séparément par l'appelant (le cas "modification"
     * doit exclure le compte courant de la vérification, ce qu'une méthode générique ne
     * peut pas savoir).
     *
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    private void validateAdminRequest(AdminUserRequest request) throws FunctionalException {
        if (isBlank(request.lastName()) || isBlank(request.firstName())
                || isBlank(request.postalAddress()) || isBlank(request.email())
                || request.role() == null) {
            throw new FunctionalException("Veuillez renseigner tous les champs obligatoires.");
        }

        // Règle PO (dossier de conception) : un organisateur est forcément rattaché à un club.
        if (request.role() == Role.ORGANIZER && (request.clubIds() == null || request.clubIds().isEmpty())) {
            throw new FunctionalException("Un organisateur doit être rattaché à au moins un club.");
        }
    }

    /**
     * Affilie un utilisateur nouvellement créé aux clubs demandés (aucun retrait à gérer,
     * l'utilisateur n'appartient encore à aucun club).
     *
     * @throws FunctionalException si un identifiant de club ne correspond à aucun club
     */
    private void assignClubs(User user, List<Long> clubIds) throws FunctionalException {
        if (clubIds == null) {
            return;
        }
        for (Long clubId : clubIds) {
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new FunctionalException("Club introuvable : id=" + clubId));
            club.getMembers().add(user);
        }
    }

    /**
     * Aligne les affiliations d'un utilisateur existant sur la liste demandée : retire les
     * clubs qui n'y figurent plus, ajoute les nouveaux.
     *
     * @throws FunctionalException si un identifiant de club ne correspond à aucun club
     */
    private void syncClubs(User user, List<Long> newClubIds) throws FunctionalException {
        List<Club> currentClubs = clubRepository.findByMembers_Id(user.getId());
        Set<Long> newIds = newClubIds == null ? Set.of() : new HashSet<>(newClubIds);

        for (Club club : currentClubs) {
            if (!newIds.contains(club.getId())) {
                club.getMembers().remove(user);
            }
        }

        Set<Long> currentIds = currentClubs.stream().map(Club::getId).collect(Collectors.toSet());
        for (Long clubId : newIds) {
            if (!currentIds.contains(clubId)) {
                Club club = clubRepository.findById(clubId)
                        .orElseThrow(() -> new FunctionalException("Club introuvable : id=" + clubId));
                club.getMembers().add(user);
            }
        }
    }

    /**
     * Génère un mot de passe temporaire respectant les mêmes critères de robustesse que
     * ceux imposés au signup self-service (12 caractères, 4 types), sans jamais le faire
     * choisir à l'admin (SFG §2.28.1.4).
     */
    private String generateTemporaryPassword() {
        List<Character> chars = new ArrayList<>();
        chars.add(PWD_UPPER.charAt(RANDOM.nextInt(PWD_UPPER.length())));
        chars.add(PWD_LOWER.charAt(RANDOM.nextInt(PWD_LOWER.length())));
        chars.add(PWD_DIGITS.charAt(RANDOM.nextInt(PWD_DIGITS.length())));
        chars.add(PWD_SPECIAL.charAt(RANDOM.nextInt(PWD_SPECIAL.length())));

        String all = PWD_UPPER + PWD_LOWER + PWD_DIGITS + PWD_SPECIAL;
        for (int i = chars.size(); i < 12; i++) {
            chars.add(all.charAt(RANDOM.nextInt(all.length())));
        }
        Collections.shuffle(chars, RANDOM);

        StringBuilder password = new StringBuilder(chars.size());
        chars.forEach(password::append);
        return password.toString();
    }

    /**
     * Contrôles métier de la création de compte (CU5, SFG §2.8.1.5/2.8.1.6).
     * <p>
     * L'acceptation CGU/RGPD (règle métier n°11) n'est pas vérifiée ici : gérée côté front
     * (case à cocher), non stockée en base — décision déjà actée pour ce CU.
     *
     * @param request informations à valider
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    private void validate(CreateUserRequest request) throws FunctionalException {
        if (isBlank(request.lastName()) || isBlank(request.firstName())
                || isBlank(request.postalAddress()) || isBlank(request.email())
                || isBlank(request.password())) {
            throw new FunctionalException("Veuillez renseigner tous les champs obligatoires.");
        }

        if (!EMAIL_PATTERN.matcher(request.email()).matches()) {
            throw new FunctionalException("Veuillez renseigner une adresse email valide.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new FunctionalException("Cette adresse email est déjà utilisée.");
        }

        if (!isStrongPassword(request.password())) {
            throw new FunctionalException("Le mot de passe ne respecte pas les critères de sécurité requis.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 12 caractères minimum + au moins une majuscule, une minuscule, un chiffre et un
     * caractère spécial (CU5, règles métier n°4 à 8).
     */
    private boolean isStrongPassword(String password) {
        if (password.length() < 12) {
            return false;
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
