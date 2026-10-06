package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.CreateEventRequest;
import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.EventSearchCriteria;
import fr.CDA.GHE.dto.UpdateEventRequest;
import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.dto.MyRegistrationDto;
import fr.CDA.GHE.dto.OrganizerEventDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.repository.CommentRepository;
import fr.CDA.GHE.repository.RegistrationRepository;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.exception.ForbiddenException;
import fr.CDA.GHE.mapper.EventMapper;

import fr.CDA.GHE.util.CurrentUser;

import fr.CDA.GHE.repository.EventRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import fr.CDA.GHE.dto.ImageDto;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Service gérant les événements.
 */
@Service
public class EventService {

    /**
     * Repository permettant l'accès aux événements en base de données.
     */
    private final EventRepository eventRepository;

    /**
     * Mapper utilisé pour convertir les événements en DTO.
     */
    private final EventMapper eventMapper;

    /**
     * Repository permettant l'accès aux utilisateurs en base de données.
     */
    private final UserRepository userRepository;

    /**
     * Repository permettant l'accès aux clubs en base de données.
     */
    private final ClubRepository clubRepository;

    /**
     * Repository permettant l'accès aux inscriptions.
     */
    private final RegistrationRepository registrationRepository;

    /**
     * Repository permettant l'accès aux commentaires.
     */
    private final CommentRepository commentRepository;

    /**
     * Service utilisé pour envoyer les emails transactionnels.
     */
    private final EmailService emailService;

    /**
     * Service permettant de gérer les images associées aux événements.
     */
    private final ImageService imageService;

    /**
     * Initialise le service avec ses dépendances.
     *
     * @param eventRepository        repository d'accès aux événements
     * @param eventMapper            mapper permettant de convertir un événement en DTO
     * @param userRepository         repository d'accès aux utilisateurs
     * @param clubRepository         repository d'accès aux clubs
     * @param registrationRepository repository d'accès aux inscriptions
     * @param commentRepository      repository d'accès aux commentaires
     * @param emailService           service d'envoi des emails transactionnels
     * @param imageService           service de gestion des images
     */
    public EventService(
            EventRepository eventRepository,
            EventMapper eventMapper,
            UserRepository userRepository,
            ClubRepository clubRepository,
            RegistrationRepository registrationRepository,
            CommentRepository commentRepository,
            EmailService emailService,
            ImageService imageService
    ) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.registrationRepository = registrationRepository;
        this.commentRepository = commentRepository;
        this.emailService = emailService;
        this.imageService = imageService;
    }

    /**
     * Retourne les événements destinés à la page publique de consultation.
     * <p>
     * Les événements publiés sont regroupés par catégorie et triés
     * chronologiquement par date de début.
     * Les événements terminés sont placés dans la rubrique des événements passés.
     * </p>
     *
     * @return les différentes listes d'événements à afficher
     */
    @Transactional(readOnly = true)
    public EventListDto getPublicEvents() {

        List<Event> publishedEvents =
                eventRepository.findByStatus(EventStatus.PUBLISHED);

        List<Event> finishedEvents =
                eventRepository.findByStatus(EventStatus.FINISHED);

        List<Long> allEventIds = Stream.concat(publishedEvents.stream(), finishedEvents.stream())
                .map(Event::getId)
                .toList();

        Map<Long, String> coverImageUrls = imageService.findCoverImageUrlsByEventIds(allEventIds);

        List<EventCardDto> cultureEvents =
                filterAndSortByCategory(publishedEvents, Category.CULTURE, coverImageUrls);

        List<EventCardDto> leisureEvents =
                filterAndSortByCategory(publishedEvents, Category.LEISURE, coverImageUrls);

        List<EventCardDto> sportEvents =
                filterAndSortByCategory(publishedEvents, Category.SPORT, coverImageUrls);

        List<EventCardDto> pastEvents = finishedEvents.stream()
                .sorted(Comparator.comparing(Event::getStartDateTime))
                .map(event -> eventMapper.toCardDto(event, coverImageUrls.get(event.getId())))
                .toList();

        return new EventListDto(
                cultureEvents,
                leisureEvents,
                sportEvents,
                pastEvents
        );
    }

    /**
     * Retourne tous les événements créés par l'organisateur actuellement authentifié, tous
     * statuts confondus (EVT-05 — contrairement à {@link #getPublicEvents()}, réservé aux
     * PUBLISHED/FINISHED), triés par date de début la plus récente en premier.
     *
     * @return les événements de l'organisateur, sous forme de cartes de gestion
     */
    @Transactional(readOnly = true)
    public List<OrganizerEventDto> getMyEvents() {

        User organizer = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        return eventRepository.findByOrganizer(organizer).stream()
                .sorted(Comparator.comparing(Event::getStartDateTime).reversed())
                .map(event -> eventMapper.toOrganizerDto(
                        event,
                        registrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED)
                ))
                .toList();
    }

    /**
     * Recherche les événements publiés correspondant aux critères fournis (CU2).
     * <p>
     * Seuls les événements {@link EventStatus#PUBLISHED} sont retournés. Chaque critère est
     * facultatif : seuls ceux effectivement renseignés sont appliqués, en complément du filtre
     * de visibilité qui s'applique systématiquement.
     * </p>
     *
     * @param criteria critères de recherche (tous facultatifs)
     * @return les événements correspondants, triés chronologiquement, sous forme de cartes
     */
    @Transactional(readOnly = true)
    public List<EventCardDto> searchEvents(EventSearchCriteria criteria) {

        Specification<Event> specification =
                Specification.where(EventSpecifications.hasStatus(EventStatus.PUBLISHED));

        if (criteria.category() != null) {
            specification = specification.and(EventSpecifications.hasCategory(criteria.category()));
        }

        if (criteria.minPrice() != null) {
            specification = specification.and(EventSpecifications.hasMinPrice(criteria.minPrice()));
        }

        if (criteria.maxPrice() != null) {
            specification = specification.and(EventSpecifications.hasMaxPrice(criteria.maxPrice()));
        }

        if (criteria.location() != null && !criteria.location().isBlank()) {
            specification = specification.and(EventSpecifications.hasLocation(criteria.location()));
        }

        if (criteria.startDate() != null) {
            specification = specification.and(
                    EventSpecifications.startsOnOrAfter(criteria.startDate().atStartOfDay())
            );
        }

        if (criteria.endDate() != null) {
            specification = specification.and(
                    EventSpecifications.startsOnOrBefore(criteria.endDate().atTime(LocalTime.MAX))
            );
        }

        if (criteria.keywords() != null && !criteria.keywords().isBlank()) {
            specification = specification.and(EventSpecifications.hasKeywords(criteria.keywords()));
        }

        List<Event> events = eventRepository.findAll(specification).stream()
                .sorted(Comparator.comparing(Event::getStartDateTime))
                .toList();

        Map<Long, String> coverImageUrls = imageService.findCoverImageUrlsByEventIds(
                events.stream().map(Event::getId).toList()
        );

        return events.stream()
                .map(event -> eventMapper.toCardDto(event, coverImageUrls.get(event.getId())))
                .toList();
    }

    /**
     * Retourne le détail d'un événement accessible à l'utilisateur courant.
     * <p>
     * Les événements publiés et terminés sont accessibles publiquement.
     * Un événement brouillon est accessible uniquement à son organisateur.
     * Les autres événements sont considérés comme non accessibles.
     * </p>
     *
     * @param id identifiant de l'événement recherché
     * @return le détail de l'événement
     * @throws NotFoundException si l'événement n'existe pas ou n'est pas accessible
     */
    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Événement introuvable")
                );

        if (!isEventAccessible(event)) {
            throw new NotFoundException("Événement introuvable");
        }

        long registeredCount = registrationRepository.countByEventAndStatus(
                event,
                RegistrationStatus.REGISTERED
        );
        long remainingSeats = Math.max(0, event.getMaxSeats() - registeredCount);
        long waitingCount = registrationRepository.countByEventAndStatus(
                event,
                RegistrationStatus.WAITING_LIST
        );

        User currentUser = currentUserOrNull();
        boolean owner = currentUser != null
                && currentUser.getId().equals(event.getOrganizer().getId());
        MyRegistrationDto myRegistration = currentUser == null
                ? null
                : findMyRegistration(currentUser, event);

        List<ImageDto> gallery = imageService.findGalleryForEvent(id);

        return eventMapper.toDetailResponse(
                event, remainingSeats, waitingCount, owner, myRegistration, gallery
        );
    }

    /**
     * Retrouve l'inscription de l'utilisateur à l'événement, avec sa position
     * s'il est en liste d'attente.
     *
     * @param user  utilisateur connecté
     * @param event événement consulté
     * @return l'inscription de l'utilisateur, {@code null} s'il n'est pas inscrit
     */
    private MyRegistrationDto findMyRegistration(User user, Event event) {
        return registrationRepository.findByUserAndEvent(user, event)
                .map(registration -> new MyRegistrationDto(
                        registration.getStatus(),
                        waitingPosition(registration, event)
                ))
                .orElse(null);
    }

    /**
     * Calcule la position d'une inscription en liste d'attente : le nombre de personnes
     * en attente inscrites avant elle, plus un.
     *
     * @param registration inscription concernée
     * @param event        événement consulté
     * @return la position, {@code null} si l'inscription n'est pas en liste d'attente
     */
    private Integer waitingPosition(Registration registration, Event event) {

        if (registration.getStatus() != RegistrationStatus.WAITING_LIST) {
            return null;
        }

        long ahead = registrationRepository.countByEventAndStatusAndRegistrationDateBefore(
                event,
                RegistrationStatus.WAITING_LIST,
                registration.getRegistrationDate()
        );

        return (int) ahead + 1;
    }

    /**
     * Crée un nouvel événement pour l'organisateur actuellement authentifié.
     *
     * @param request données saisies pour la création de l'événement
     * @return l'événement créé
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @Transactional
    public EventDto createEvent(CreateEventRequest request) throws FunctionalException {

        validateCreateRequest(request);

        User organizer = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (organizer.getRole() != Role.ORGANIZER) {
            throw new FunctionalException(
                    "Vous n'êtes pas autorisé à créer un événement."
            );
        }

        Club club = clubRepository.findByMembers_Id(organizer.getId()).stream()
                .filter(affiliatedClub -> affiliatedClub.getId().equals(request.clubId()))
                .findFirst()
                .orElseThrow(() -> new FunctionalException(
                        "Vous n'êtes pas autorisé à créer un événement pour ce club."
                ));

        if (club.getValidityEndDate() != null) {
            throw new FunctionalException(
                    "Vous n'êtes pas autorisé à créer un événement pour ce club."
            );
        }

        Event event = new Event(
                request.title(),
                request.description(),
                request.location(),
                request.startDateTime(),
                request.endDateTime(),
                request.affiliatedPrice(),
                request.nonAffiliatedPrice(),
                request.maxSeats(),
                request.category(),
                organizer,
                club
        );

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toDto(savedEvent);
    }

    /**
     * Modifie un événement appartenant à l'organisateur actuellement authentifié.
     *
     * @param id      identifiant de l'événement à modifier
     * @param request nouvelles données de l'événement
     * @return l'événement mis à jour
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @Transactional
    public EventDto updateEvent(Long id, UpdateEventRequest request)
            throws FunctionalException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        checkOwnership(event, "modifier cet événement");

        // Un événement FINISHED reste modifiable uniquement pour ses images : cette exception est
        // déjà satisfaite par la conception (POST/DELETE /events/{id}/images, voir ImageService,
        // ne passent pas par cette méthode et ignorent volontairement le statut de l'événement).
        if (event.getStatus() == EventStatus.FINISHED) {
            throw new FunctionalException(
                    "Un événement passé ne peut plus être modifié, à l'exception de ses images."
            );
        }

        validateUpdateRequest(event, request);

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setStartDateTime(request.startDateTime());
        event.setEndDateTime(request.endDateTime());
        event.setAffiliatedPrice(request.affiliatedPrice());
        event.setNonAffiliatedPrice(request.nonAffiliatedPrice());
        event.setMaxSeats(request.maxSeats());
        event.setCategory(request.category());

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toDto(savedEvent);
    }

    /**
     * Vérifie les données utilisées pour modifier un événement.
     *
     * @param event   événement actuellement enregistré
     * @param request nouvelles données à contrôler
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    private void validateUpdateRequest(
            Event event,
            UpdateEventRequest request
    ) throws FunctionalException {

        if (request == null
                || isBlank(request.title())
                || isBlank(request.description())
                || isBlank(request.location())
                || request.startDateTime() == null
                || request.affiliatedPrice() == null
                || request.nonAffiliatedPrice() == null
                || request.maxSeats() == null
                || request.category() == null) {

            throw new FunctionalException(
                    "Veuillez renseigner tous les champs obligatoires."
            );
        }

        if (!request.startDateTime().equals(event.getStartDateTime())
                && !request.startDateTime().isAfter(LocalDateTime.now())) {

            throw new FunctionalException(
                    "La date de début doit être postérieure à la date et à l'heure actuelles."
            );
        }

        if (request.endDateTime() != null
                && !request.endDateTime().isAfter(request.startDateTime())) {

            throw new FunctionalException(
                    "La date de fin doit être postérieure à la date de début."
            );
        }

        if (request.maxSeats() <= 0) {
            throw new FunctionalException(
                    "Le nombre maximal de places doit être strictement supérieur à zéro."
            );
        }

        if (request.affiliatedPrice().compareTo(BigDecimal.ZERO) < 0
                || request.nonAffiliatedPrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new FunctionalException(
                    "Les tarifs doivent être supérieurs ou égaux à zéro."
            );
        }
    }

    /**
     * Filtre les événements selon une catégorie, les trie par date de début
     * puis les convertit en DTO de carte.
     *
     * @param events   événements à traiter
     * @param category catégorie recherchée
     * @return les événements de la catégorie, triés chronologiquement
     */
    private List<EventCardDto> filterAndSortByCategory(
            List<Event> events,
            Category category,
            Map<Long, String> coverImageUrls
    ) {
        return events.stream()
                .filter(event -> event.getCategory() == category)
                .sorted(Comparator.comparing(Event::getStartDateTime))
                .map(event -> eventMapper.toCardDto(event, coverImageUrls.get(event.getId())))
                .toList();
    }

    /**
     * Vérifie que l'utilisateur actuellement authentifié est l'organisateur propriétaire de
     * l'événement (SEC-03) : seul lui peut modifier/publier/annuler/supprimer son événement.
     *
     * @param event  événement concerné
     * @param action décrit l'action refusée dans le message d'erreur (ex. "publier cet événement")
     * @throws NotFoundException  si l'utilisateur authentifié n'existe plus en base
     * @throws ForbiddenException si l'utilisateur n'est pas l'organisateur propriétaire
     */
    private void checkOwnership(Event event, String action) {

        User organizer = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (organizer.getRole() != Role.ORGANIZER
                || !event.getOrganizer().getId().equals(organizer.getId())) {

            throw new ForbiddenException("Vous n’êtes pas autorisé à " + action + ".");
        }
    }

    /**
     * Vérifie si un événement peut être consulté par l'utilisateur courant.
     * <p>
     * Package-private et statique (plutôt que private) pour être réutilisée telle quelle par
     * {@link CommentService}, qui doit appliquer exactement la même règle de visibilité que la
     * consultation du détail d'un événement (COM-01) : les commentaires d'un brouillon ne
     * doivent pas être accessibles à qui ne pourrait pas consulter l'événement lui-même.
     * </p>
     *
     * @param event événement dont la visibilité doit être vérifiée
     * @return {@code true} si l'événement est accessible, sinon {@code false}
     */
    static boolean isEventAccessible(Event event) {

        if (event.getStatus() == EventStatus.PUBLISHED
                || event.getStatus() == EventStatus.FINISHED) {
            return true;
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            return false;
        }

        User authenticatedUser = currentUserOrNull();

        if (authenticatedUser == null) {
            return false;
        }

        return authenticatedUser.getId().equals(event.getOrganizer().getId());
    }

    /**
     * Retourne l'utilisateur authentifié de la requête en cours.
     *
     * @return l'utilisateur connecté, {@code null} si la requête est anonyme
     */
    private static User currentUserOrNull() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return null;
        }

        return authenticatedUser;
    }

    /**
     * Vérifie les données nécessaires à la création d'un événement.
     *
     * @param request données à contrôler
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    private void validateCreateRequest(CreateEventRequest request)
            throws FunctionalException {

        if (request == null
                || isBlank(request.title())
                || isBlank(request.description())
                || isBlank(request.location())
                || request.startDateTime() == null
                || request.affiliatedPrice() == null
                || request.nonAffiliatedPrice() == null
                || request.maxSeats() == null
                || request.category() == null
                || request.clubId() == null) {

            throw new FunctionalException(
                    "Veuillez renseigner tous les champs obligatoires."
            );
        }

        if (!request.startDateTime().isAfter(LocalDateTime.now())) {
            throw new FunctionalException(
                    "La date de début doit être postérieure à la date et à l'heure actuelles."
            );
        }

        if (request.endDateTime() != null
                && !request.endDateTime().isAfter(request.startDateTime())) {

            throw new FunctionalException(
                    "La date de fin doit être postérieure à la date de début."
            );
        }

        if (request.maxSeats() <= 0) {
            throw new FunctionalException(
                    "Le nombre maximal de places doit être strictement supérieur à zéro."
            );
        }

        if (request.affiliatedPrice().compareTo(BigDecimal.ZERO) < 0
                || request.nonAffiliatedPrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new FunctionalException(
                    "Les tarifs doivent être supérieurs ou égaux à zéro."
            );
        }
    }

    /**
     * Vérifie si une chaîne est absente ou vide.
     *
     * @param value chaîne à vérifier
     * @return {@code true} si la chaîne est nulle, vide ou ne contient que des espaces
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Publie un événement appartenant à l'organisateur actuellement authentifié.
     *
     * @param id identifiant de l'événement à publier
     * @return l'événement publié
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @Transactional
    public EventDto publishEvent(Long id) throws FunctionalException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        checkOwnership(event, "publier cet événement");

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new FunctionalException(
                    "Cet événement ne peut pas être publié dans son état actuel."
            );
        }

        event.publish();

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toDto(savedEvent);
    }

    /**
     * Termine manuellement un événement appartenant à l'organisateur authentifié.
     *
     * @param id identifiant de l'événement à terminer
     * @return l'événement terminé
     * @throws FunctionalException si le changement de statut n'est pas autorisé
     */
    @Transactional
    public EventDto finishEvent(Long id) throws FunctionalException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        checkOwnership(event, "modifier le statut de cet événement");

        if (event.getStatus() != EventStatus.PUBLISHED
                || event.getStartDateTime() == null
                || (event.getEndDateTime() != null
                && !event.getEndDateTime().isAfter(event.getStartDateTime()))) {

            throw new FunctionalException(
                    "Ce changement de statut n’est pas autorisé."
            );
        }

        event.finish();

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toDto(savedEvent);
    }

    /**
     * Annule un événement publié appartenant à l'organisateur authentifié.
     * <p>
     * L'annulation est utilisée lorsqu'un événement publié possède au moins
     * un inscrit. L'événement est conservé en base au statut CANCELLED et
     * les inscrits ainsi que les personnes en liste d'attente sont informés.
     * </p>
     *
     * @param id identifiant de l'événement à annuler
     * @return l'événement annulé
     * @throws FunctionalException si l'événement ne peut pas être annulé
     */
    @Transactional
    public EventDto cancelEvent(Long id) throws FunctionalException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        checkOwnership(event, "annuler cet événement");

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new FunctionalException(
                    "Cet événement ne peut pas être annulé dans son état actuel."
            );
        }

        long registeredCount = registrationRepository.countByEventAndStatus(
                event,
                RegistrationStatus.REGISTERED
        );

        if (registeredCount == 0) {
            throw new FunctionalException(
                    "Cet événement ne peut pas être annulé dans son état actuel."
            );
        }

        List<Registration> registrations =
                registrationRepository.findByEvent(event);

        event.cancel();

        Event savedEvent = eventRepository.save(event);

        for (Registration registration : registrations) {
            emailService.sendEventCancelledEmail(
                    registration.getUser().getEmail(),
                    event.getTitle()
            );
        }

        return eventMapper.toDto(savedEvent);
    }

    /**
     * Supprime définitivement un événement lorsque les règles métier
     * autorisent une suppression physique.
     *
     * @param id identifiant de l'événement à supprimer
     * @throws FunctionalException si l'événement ne peut pas être supprimé
     */
    @Transactional
    public void deleteEvent(Long id) throws FunctionalException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        checkOwnership(event, "supprimer cet événement");

        if (event.getStatus() == EventStatus.PUBLISHED) {

            long registeredCount = registrationRepository.countByEventAndStatus(
                    event,
                    RegistrationStatus.REGISTERED
            );

            if (registeredCount > 0) {
                throw new FunctionalException(
                        "Cet événement ne peut pas être supprimé car il possède des inscrits. "
                                + "Il doit être annulé."
                );
            }

        } else if (event.getStatus() != EventStatus.DRAFT) {

            throw new FunctionalException(
                    "Cet événement ne peut pas être supprimé dans son état actuel."
            );
        }

        registrationRepository.deleteAll(
                registrationRepository.findByEvent(event)
        );

        commentRepository.deleteAllByEvent_Id(id);

        imageService.removeAllImagesForEvent(id);

        eventRepository.delete(event);
    }
}
