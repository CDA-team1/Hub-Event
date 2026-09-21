package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.ClubMapper;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

/**
 * Gère la création, la modification, la suppression et la consultation des clubs.
 * <p>
 * Réservé à l'administrateur (contrôle du rôle fait au niveau du controller via
 * {@code @Secured}, voir SEC-02).
 * </p>
 */
@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;
    private final EventRepository eventRepository;

    public ClubService(ClubRepository clubRepository, ClubMapper clubMapper, EventRepository eventRepository){
        this.clubRepository = clubRepository;
        this.clubMapper = clubMapper;
        this.eventRepository = eventRepository;
    }

    /**
     * Retourne une page de clubs.
     *
     * @param pageable pagination demandée
     * @return la page de clubs correspondante
     */
    @Transactional(readOnly = true)
    public PageDto<ClubDto> extractAll(Pageable pageable){
        Page<Club> page = clubRepository.findAll(pageable);
        return new PageDto<>(
                clubMapper.toDtoList(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    /**
     * Retourne un club par son identifiant.
     *
     * @param id identifiant du club
     * @return le club trouvé
     * @throws NotFoundException si aucun club ne correspond à l'identifiant
     */
    @Transactional(readOnly = true)
    public ClubDto extractById(Long id){
        Club club = clubRepository.findById(id).orElseThrow(() -> new NotFoundException("Club introuvable"));
        return clubMapper.toDto(club);
    }

    /**
     * Crée un nouveau club (CU24).
     *
     * @param dto informations saisies pour le club à créer
     * @return le club créé
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @Transactional
    public ClubDto create(ClubDto dto) throws FunctionalException{
        validate(dto);

        Club club = new Club(dto.name(), dto.category(), dto.postalAddress(), dto.email(), dto.phone());
        Club created = clubRepository.save(club);
        return clubMapper.toDto(created);
    }

    /**
     * Modifie un club existant (CU24).
     * <p>
     * La date de fin de validité n'est pas modifiable ici : elle n'est jamais saisie par
     * l'utilisateur, mais calculée par le système lors d'une suppression (voir {@link #delete}).
     *
     * @param id  identifiant du club à modifier
     * @param dto nouvelles informations du club
     * @return le club modifié
     * @throws NotFoundException   si aucun club ne correspond à l'identifiant
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @Transactional
    public ClubDto update(Long id, ClubDto dto) throws FunctionalException{
        validate(dto);

        Club club = clubRepository.findById(id).orElseThrow(() -> new NotFoundException("Club introuvable"));

        club.setName(dto.name());
        club.setCategory(dto.category());
        club.setPostalAddress(dto.postalAddress());
        club.setEmail(dto.email());
        club.setPhone(dto.phone());
        // validityEndDate volontairement ignorée : non saisissable, calculée par le système.

        return clubMapper.toDto(club);
    }

    /**
     * Supprime un club et ses conséquences en cascade (CU24, SFG §2.27, règles n°2 à 6).
     * <p>
     * « Supprimer » ne retire pas la ligne en base : {@code events.club_id} est une FK non
     * nullable, et on veut préserver l'historique des événements du club (même principe que
     * l'anonymisation d'un compte {@code User} plutôt que sa suppression). La suppression se
     * traduit par {@link Club#endClubAffiliation}, déjà le signal utilisé ailleurs (voir
     * {@code EventService.createEvent}) pour refuser toute nouvelle action sur un club qui n'est
     * plus affilié.
     * <p>
     * Les affiliations du club sont supprimées ; un membre sans autre club devient non affilié ;
     * un organisateur sans autre club perd son rôle (règle n°5, même logique que
     * {@code UserService.updateMemberAffiliations}, CU27) ; les événements futurs publiés du
     * club sont annulés (règle n°6, même logique que
     * {@code AnonymizationRequestService.cancelUpcomingPublishedEvents}, CU29). Les événements
     * passés, déjà annulés ou en brouillon ne sont pas concernés : ils gardent leur référence au
     * club, qui continue d'exister en base.
     *
     * @param id identifiant du club à supprimer
     * @throws NotFoundException   si aucun club ne correspond à l'identifiant
     * @throws FunctionalException si le club est déjà supprimé (fin de validité déjà renseignée)
     */
    @Transactional
    public void delete(Long id) throws FunctionalException {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Club introuvable"));

        if (club.getValidityEndDate() != null) {
            throw new FunctionalException("Ce club est déjà supprimé.");
        }

        LocalDateTime now = LocalDateTime.now();

        eventRepository.findByClub_Id(id).stream()
                .filter(event -> event.getStatus() == EventStatus.PUBLISHED
                        && event.getStartDateTime().isAfter(now))
                .forEach(Event::cancel);

        for (User member : new HashSet<>(club.getMembers())) {
            club.removeMember(member);

            if (member.getRole() == Role.ORGANIZER && member.getClubs().isEmpty()) {
                member.setRole(Role.MEMBER);
            }
        }

        club.endClubAffiliation(now.toLocalDate());
    }

    private void validate(ClubDto dto) throws FunctionalException {
        if (isBlank(dto.name())) {
            throw new FunctionalException("Le nom du club est obligatoire");
        }
        if (dto.category() == null) {
            throw new FunctionalException("La catégorie du club est obligatoire");
        }
        if (isBlank(dto.postalAddress())) {
            throw new FunctionalException("L'adresse postale du club est obligatoire");
        }
        if (isBlank(dto.email())) {
            throw new FunctionalException("L'adresse email du club est obligatoire");
        }
        if (isBlank(dto.phone())) {
            throw new FunctionalException("Le numéro de téléphone du club est obligatoire");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
