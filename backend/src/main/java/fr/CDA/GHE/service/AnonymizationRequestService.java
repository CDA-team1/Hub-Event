package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.AnonymizationDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.AdminAnonymizationDto;
import fr.CDA.GHE.entity.AnonymizationRequest;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.RequestStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.AnonymizationRequestMapper;
import fr.CDA.GHE.repository.AnonymizationRequestRepository;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.util.AnonymizationUtils;
import fr.CDA.GHE.util.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Gère les demandes d'anonymisation des utilisateurs (CU14, CU28, CU29).
 */
@Service
public class AnonymizationRequestService {

    private static final Logger log = LoggerFactory.getLogger(AnonymizationRequestService.class);

    private final AnonymizationRequestRepository anonymizationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final AnonymizationRequestMapper anonymizationRequestMapper;
    private final PasswordEncoder passwordEncoder;

    public AnonymizationRequestService(AnonymizationRequestRepository anonymizationRequestRepository,
                                        UserRepository userRepository,
                                        EventRepository eventRepository,
                                        AnonymizationRequestMapper anonymizationRequestMapper,
                                        PasswordEncoder passwordEncoder,
                                        UserService userService
) {
        this.anonymizationRequestRepository = anonymizationRequestRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.anonymizationRequestMapper = anonymizationRequestMapper;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * Enregistre une demande d'anonymisation pour l'utilisateur connecté (CU14, SFG §2.17).
     * <p>
     * La demande ne provoque pas immédiatement l'anonymisation du compte : elle reste
     * {@link RequestStatus#PENDING} jusqu'à ce qu'un administrateur la valide (CU29).
     *
     * @return la demande créée
     * @throws FunctionalException si le compte n'est pas actif, ou si une demande existe déjà
     */
    @Transactional
    public AnonymizationDto createRequest() throws FunctionalException {
        User user = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new FunctionalException("Votre compte doit être actif pour demander l'anonymisation de vos données.");
        }

        if (anonymizationRequestRepository.findByUser(user).isPresent()) {
            throw new FunctionalException("Une demande d'anonymisation est déjà en cours pour votre compte.");
        }

        AnonymizationRequest request = new AnonymizationRequest(user, RequestStatus.PENDING, LocalDateTime.now());
        AnonymizationRequest created = anonymizationRequestRepository.save(request);

        log.info("CREATION demande d'anonymisation : id={} userEmail={}", created.getId(), user.getEmail());

        return anonymizationRequestMapper.toDto(created);
    }

    /**
     * Retourne une page de demandes d'anonymisation, avec les informations de l'utilisateur
     * concerné pour chacune (CU28, SFG §2.31).
     *
     * @param pageable pagination demandée
     * @return la page de demandes correspondante
     */
    @Transactional(readOnly = true)
    public PageDto<AdminAnonymizationDto> extractAll(Pageable pageable) {
        Page<AnonymizationRequest> page =
            anonymizationRequestRepository.findByStatus(RequestStatus.PENDING, pageable);

        List<AdminAnonymizationDto> content = page.getContent()
            .stream()
            .map(request -> new AdminAnonymizationDto(
                    request.getId(),
                    userService.extractById(request.getUser().getId()),
                    request.getStatus(),
                    request.getRequestDate()
            ))
            .toList();

    return new PageDto<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
    );
}

    /**
     * Valide une demande d'anonymisation et anonymise le compte concerné (CU29, SFG §2.32).
     * <p>
     * Les données personnelles de l'utilisateur sont remplacées par des valeurs aléatoires et
     * son statut passe à {@link AccountStatus#ANONYMIZED} (connexion impossible dès lors,
     * cf. {@code AuthService.login}). Ses commentaires déjà publiés sont conservés (aucune
     * suppression) : l'affichage de leur auteur sous la mention « SUPPRIMÉ » est géré par
     * {@code CommentMapper.formatAuthorDisplayName}, qui vérifie ce statut. Si l'utilisateur
     * est organisateur, ses évènements publiés à venir sont annulés, sans transfert à un autre
     * organisateur (règle CU29 n°7).
     *
     * @param id identifiant de la demande à valider
     * @return la demande validée
     * @throws NotFoundException   si aucune demande ne correspond à l'identifiant
     * @throws FunctionalException si la demande a déjà été traitée
     */
    @Transactional
    public AnonymizationDto validateRequest(Long id) throws FunctionalException {
        AnonymizationRequest request = anonymizationRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Demande d'anonymisation introuvable"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new FunctionalException("Aucune demande d'anonymisation valide n'est associée à cet utilisateur.");
        }

        User user = request.getUser();

        AnonymizationUtils.anonymize(user);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setStatus(AccountStatus.ANONYMIZED);

        if (user.getRole() == Role.ORGANIZER) {
            cancelUpcomingPublishedEvents(user);
        }

        request.setStatus(RequestStatus.VALIDATED);

        log.info("VALIDATION demande d'anonymisation : id={} userId={}", request.getId(), user.getId());

        // Entités gérées : le dirty checking JPA persiste les changements au commit.
        return anonymizationRequestMapper.toDto(request);
    }

    private void cancelUpcomingPublishedEvents(User organizer) {
        LocalDateTime now = LocalDateTime.now();
        List<Event> upcomingPublishedEvents = eventRepository.findByOrganizerAndStatus(organizer, EventStatus.PUBLISHED)
                .stream()
                .filter(event -> event.getStartDateTime().isAfter(now))
                .toList();

        upcomingPublishedEvents.forEach(Event::cancel);
    }
}
