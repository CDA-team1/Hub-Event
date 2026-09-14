package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.CreateUserRequest;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.mapper.UserMapper;
import fr.CDA.GHE.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Gère le cycle de vie des comptes utilisateurs.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // Format simplifié, suffisant pour un contrôle serveur (CU5, règle métier n°3).
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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

        User created = userRepository.save(user);
        log.info("CREATION compte non affilié : id={} email={}", created.getId(), created.getEmail());

        // TODO SIGNUP : envoyer l'email de validation (CU5, règle métier n°10) et brancher
        // GET /users/activation dessus. Dépend d'un service d'envoi d'email pas encore
        // implémenté (pas de tâche identifiée à ce jour) — le compte reste INACTIF en
        // attendant, conformément à la règle métier.

        return userMapper.toDto(created);
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
