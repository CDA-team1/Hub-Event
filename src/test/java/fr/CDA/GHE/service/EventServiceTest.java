package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.dto.CreateEventRequest;
import fr.CDA.GHE.dto.UpdateEventRequest;
import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import fr.CDA.GHE.exception.ForbiddenException;
import fr.CDA.GHE.repository.RegistrationRepository;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

/**
 * Tests du service de gestion des événements.
 */
@SpringBootTest
@ActiveProfiles("test")
class EventServiceTest {

  /**
   * Service de gestion des événements testé.
   */
  @Autowired
  private EventService eventService;

  /**
   * Repository des événements remplacé par un mock
   * dans le contexte Spring de test.
   */
  @MockitoBean
  private EventRepository eventRepository;

  /**
   * Repository des utilisateurs remplacé par un mock
   * dans le contexte Spring de test.
   */
  @MockitoBean
  private UserRepository userRepository;

  /**
   * Repository des clubs remplacé par un mock
   * dans le contexte Spring de test.
   */
  @MockitoBean
  private ClubRepository clubRepository;

  /**
   * Repository des inscriptions remplacé par un mock
   * dans le contexte Spring de test.
   */
  @MockitoBean
  private RegistrationRepository registrationRepository;

  /**
   * Service d'envoi d'emails remplacé par un mock
   * dans le contexte Spring de test.
   */
  @MockitoBean
  private EmailService emailService;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  /**
   * Vérifie que les événements publiés sont regroupés par catégorie
   * et triés chronologiquement par date de début.
   */
  @Test
  void shouldGroupPublishedEventsByCategoryAndSortThemChronologically() {

    Event cultureLater = createEvent(
        "Concert du soir",
        Category.CULTURE,
        EventStatus.PUBLISHED,
        LocalDateTime.of(2026, 11, 20, 20, 0)
    );

    Event cultureEarlier = createEvent(
        "Exposition",
        Category.CULTURE,
        EventStatus.PUBLISHED,
        LocalDateTime.of(2026, 10, 15, 10, 0)
    );

    Event sportEvent = createEvent(
        "Tournoi",
        Category.SPORT,
        EventStatus.PUBLISHED,
        LocalDateTime.of(2026, 10, 25, 14, 0)
    );

    when(eventRepository.findByStatus(EventStatus.PUBLISHED))
        .thenReturn(List.of(cultureLater, sportEvent, cultureEarlier));

    when(eventRepository.findByStatus(EventStatus.FINISHED))
        .thenReturn(List.of());

    EventListDto result = eventService.getPublicEvents();

    assertEquals(2, result.cultureEvents().size());
    assertEquals("Exposition", result.cultureEvents().get(0).title());
    assertEquals("Concert du soir", result.cultureEvents().get(1).title());

    assertEquals(1, result.sportEvents().size());
    assertEquals("Tournoi", result.sportEvents().get(0).title());

    assertEquals(0, result.leisureEvents().size());
    assertEquals(0, result.pastEvents().size());
  }

  /**
   * Vérifie que les événements terminés sont placés
   * dans la rubrique des événements passés et triés chronologiquement.
   */
  @Test
  void shouldPutFinishedEventsInPastEventsAndSortThemChronologically() {

    Event finishedLater = createEvent(
        "Festival terminé",
        Category.CULTURE,
        EventStatus.FINISHED,
        LocalDateTime.of(2026, 9, 20, 18, 0)
    );

    Event finishedEarlier = createEvent(
        "Tournoi terminé",
        Category.SPORT,
        EventStatus.FINISHED,
        LocalDateTime.of(2026, 8, 10, 14, 0)
    );

    when(eventRepository.findByStatus(EventStatus.PUBLISHED))
        .thenReturn(List.of());

    when(eventRepository.findByStatus(EventStatus.FINISHED))
        .thenReturn(List.of(finishedLater, finishedEarlier));

    EventListDto result = eventService.getPublicEvents();

    assertEquals(2, result.pastEvents().size());
    assertEquals("Tournoi terminé", result.pastEvents().get(0).title());
    assertEquals("Festival terminé", result.pastEvents().get(1).title());

    assertEquals(0, result.cultureEvents().size());
    assertEquals(0, result.leisureEvents().size());
    assertEquals(0, result.sportEvents().size());
  }

  /**
   * Vérifie que le service ne recherche pas les événements
   * au statut brouillon ou annulé pour la consultation publique.
   */
  @Test
  void shouldNotRequestDraftOrCancelledEvents() {

    when(eventRepository.findByStatus(EventStatus.PUBLISHED))
        .thenReturn(List.of());

    when(eventRepository.findByStatus(EventStatus.FINISHED))
        .thenReturn(List.of());

    eventService.getPublicEvents();

    verify(eventRepository).findByStatus(EventStatus.PUBLISHED);
    verify(eventRepository).findByStatus(EventStatus.FINISHED);

    verify(eventRepository, never()).findByStatus(EventStatus.DRAFT);
    verify(eventRepository, never()).findByStatus(EventStatus.CANCELLED);
  }

  /**
   * Vérifie que le détail d'un événement publié est accessible.
   */
  @Test
  void shouldReturnPublishedEventDetail() {

    Event event = createEvent(
        "Concert",
        Category.CULTURE,
        EventStatus.PUBLISHED,
        LocalDateTime.of(2026, 11, 20, 20, 0)
    );

    when(eventRepository.findById(1L))
        .thenReturn(Optional.of(event));

    EventDetailResponse result = eventService.getEventDetail(1L);

    assertEquals("Concert", result.title());
    assertEquals("Description de test", result.description());
    assertEquals("Montpellier", result.location());
    assertEquals(
        LocalDateTime.of(2026, 11, 20, 20, 0),
        result.startDateTime()
    );
    assertEquals(BigDecimal.valueOf(10), result.affiliatedPrice());
    assertEquals(BigDecimal.valueOf(15), result.nonAffiliatedPrice());
    assertEquals(100, result.maxSeats());
  }

  /**
   * Vérifie que le détail d'un événement terminé reste accessible.
   */
  @Test
  void shouldReturnFinishedEventDetail() {

    Event event = createEvent(
        "Festival terminé",
        Category.CULTURE,
        EventStatus.FINISHED,
        LocalDateTime.of(2026, 8, 10, 18, 0)
    );

    when(eventRepository.findById(2L))
        .thenReturn(Optional.of(event));

    EventDetailResponse result = eventService.getEventDetail(2L);

    assertEquals("Festival terminé", result.title());
  }

  /**
   * Vérifie qu'un événement inexistant provoque une NotFoundException.
   */
  @Test
  void shouldThrowNotFoundExceptionWhenEventDoesNotExist() {

    when(eventRepository.findById(999L))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> eventService.getEventDetail(999L)
    );
  }

  /**
   * Vérifie que l'organisateur peut consulter son propre événement brouillon.
   */
  @Test
  void shouldReturnDraftEventDetailForItsOrganizer() {

    User organizer = mock(User.class);
    when(organizer.getId()).thenReturn(1L);

    Event event = createEvent(
        "Brouillon",
        Category.SPORT,
        EventStatus.DRAFT,
        LocalDateTime.of(2026, 12, 5, 14, 0)
    );
    event.setOrganizer(organizer);

    when(eventRepository.findById(3L))
        .thenReturn(Optional.of(event));

    authenticateAs(organizer);

    EventDetailResponse result = eventService.getEventDetail(3L);

    assertEquals("Brouillon", result.title());
  }

  /**
   * Vérifie qu'un utilisateur ne peut pas consulter
   * le brouillon d'un autre organisateur.
   */
  @Test
  void shouldThrowNotFoundExceptionForDraftOwnedByAnotherUser() {

    User organizer = mock(User.class);
    when(organizer.getId()).thenReturn(1L);

    User otherUser = mock(User.class);
    when(otherUser.getId()).thenReturn(2L);

    Event event = createEvent(
        "Brouillon privé",
        Category.LEISURE,
        EventStatus.DRAFT,
        LocalDateTime.of(2026, 12, 10, 10, 0)
    );
    event.setOrganizer(organizer);

    when(eventRepository.findById(4L))
        .thenReturn(Optional.of(event));

    authenticateAs(otherUser);

    assertThrows(
        NotFoundException.class,
        () -> eventService.getEventDetail(4L)
    );
  }

  /**
   * Vérifie qu'un visiteur non authentifié
   * ne peut pas consulter un événement brouillon.
   */
  @Test
  void shouldThrowNotFoundExceptionForDraftWhenUnauthenticated() {

    User organizer = mock(User.class);
    when(organizer.getId()).thenReturn(1L);

    Event event = createEvent(
        "Brouillon privé",
        Category.CULTURE,
        EventStatus.DRAFT,
        LocalDateTime.of(2026, 12, 15, 18, 0)
    );
    event.setOrganizer(organizer);

    when(eventRepository.findById(5L))
        .thenReturn(Optional.of(event));

    assertThrows(
        NotFoundException.class,
        () -> eventService.getEventDetail(5L)
    );
  }

  /**
   * Vérifie qu'un organisateur affilié à un club peut créer un événement.
   */
  @Test
  void shouldCreateDraftEventForAffiliatedOrganizer() throws FunctionalException {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    when(club.getId()).thenReturn(10L);
    when(club.getValidityEndDate()).thenReturn(null);

    authenticateAs(organizer);

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(clubRepository.findByMembers_Id(1L))
        .thenReturn(List.of(club));

    when(eventRepository.save(any(Event.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);
    LocalDateTime endDateTime = startDateTime.plusHours(2);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        endDateTime,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        10L
    );

    EventDto result = eventService.createEvent(request);

    assertEquals("Tournoi de tennis", result.title());
    assertEquals("Tournoi ouvert aux membres", result.description());
    assertEquals("Montpellier", result.location());
    assertEquals(startDateTime, result.startDateTime());
    assertEquals(endDateTime, result.endDateTime());
    assertEquals(BigDecimal.valueOf(10), result.affiliatedPrice());
    assertEquals(BigDecimal.valueOf(15), result.nonAffiliatedPrice());
    assertEquals(100, result.maxSeats());
    assertEquals(Category.SPORT, result.category());
    assertEquals(EventStatus.DRAFT, result.status());
    assertEquals(1L, result.organizerId());
    assertEquals(10L, result.clubId());

    verify(eventRepository).save(any(Event.class));
  }

  /**
   * Vérifie qu'un utilisateur qui n'est pas organisateur
   * ne peut pas créer un événement.
   */
  @Test
  void shouldRejectEventCreationForNonOrganizer() {

    User member = mock(User.class);

    when(member.getUsername()).thenReturn("member@test.fr");
    when(member.getRole()).thenReturn(Role.MEMBER);

    authenticateAs(member);

    when(userRepository.findByEmail("member@test.fr"))
        .thenReturn(Optional.of(member));

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime.plusHours(2),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "Vous n'êtes pas autorisé à créer un événement.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un organisateur ne peut pas créer un événement
   * pour un club auquel il n'est pas affilié.
   */
  @Test
  void shouldRejectEventCreationForUnaffiliatedClub() {

    User organizer = mock(User.class);
    Club affiliatedClub = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    when(affiliatedClub.getId()).thenReturn(20L);

    authenticateAs(organizer);

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(clubRepository.findByMembers_Id(1L))
        .thenReturn(List.of(affiliatedClub));

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime.plusHours(2),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "Vous n'êtes pas autorisé à créer un événement pour ce club.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un organisateur ne peut pas créer un événement
   * pour un club dont la validité est terminée.
   */
  @Test
  void shouldRejectEventCreationForExpiredClub() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    when(club.getId()).thenReturn(10L);
    when(club.getValidityEndDate()).thenReturn(LocalDate.now());

    authenticateAs(organizer);

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(clubRepository.findByMembers_Id(1L))
        .thenReturn(List.of(club));

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime.plusHours(2),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "Vous n'êtes pas autorisé à créer un événement pour ce club.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement ne peut pas être créé
   * avec une date de début passée.
   */
  @Test
  void shouldRejectEventCreationWithPastStartDate() {

    LocalDateTime startDateTime = LocalDateTime.now().minusDays(1);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime.plusHours(2),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "La date de début doit être postérieure à la date et à l'heure actuelles.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement ne peut pas être créé
   * avec une date de fin antérieure ou égale à la date de début.
   */
  @Test
  void shouldRejectEventCreationWithInvalidEndDate() {

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "La date de fin doit être postérieure à la date de début.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement ne peut pas être créé
   * avec un tarif affilié négatif.
   */
  @Test
  void shouldRejectEventCreationWithNegativeAffiliatedPrice() {

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime.plusHours(2),
        BigDecimal.valueOf(-1),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "Les tarifs doivent être supérieurs ou égaux à zéro.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement ne peut pas être créé
   * avec un tarif non affilié négatif.
   */
  @Test
  void shouldRejectEventCreationWithNegativeNonAffiliatedPrice() {

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime.plusHours(2),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(-1),
        100,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "Les tarifs doivent être supérieurs ou égaux à zéro.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement ne peut pas être créé
   * sans titre.
   */
  @Test
  void shouldRejectEventCreationWithoutTitle() {

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "   ",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime.plusHours(2),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "Veuillez renseigner tous les champs obligatoires.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement gratuit peut être créé
   * sans date de fin.
   */
  @Test
  void shouldCreateFreeEventWithoutEndDate() throws FunctionalException {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    when(club.getId()).thenReturn(10L);
    when(club.getValidityEndDate()).thenReturn(null);

    authenticateAs(organizer);

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(clubRepository.findByMembers_Id(1L))
        .thenReturn(List.of(club));

    when(eventRepository.save(any(Event.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "Événement gratuit",
        "Événement sans date de fin",
        "Montpellier",
        startDateTime,
        null,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        100,
        Category.CULTURE,
        10L
    );

    EventDto result = eventService.createEvent(request);

    assertEquals("Événement gratuit", result.title());
    assertEquals(null, result.endDateTime());
    assertEquals(BigDecimal.ZERO, result.affiliatedPrice());
    assertEquals(BigDecimal.ZERO, result.nonAffiliatedPrice());
    assertEquals(EventStatus.DRAFT, result.status());

    verify(eventRepository).save(any(Event.class));
  }

  /**
   * Place dans le contexte de sécurité un utilisateur authentifié.
   *
   * @param user utilisateur à placer dans le contexte de sécurité
   */
  private void authenticateAs(User user) {

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            user,
            null,
            List.of()
        )
    );
  }

  /**
   * Crée un événement destiné aux tests avec le statut demandé.
   *
   * @param title         titre de l'événement
   * @param category      catégorie de l'événement
   * @param status        statut attendu pour le test
   * @param startDateTime date et heure de début
   * @return l'événement créé pour le test
   */
  private Event createEvent(
      String title,
      Category category,
      EventStatus status,
      LocalDateTime startDateTime
  ) {
    Event event = new Event(
        title,
        "Description de test",
        "Montpellier",
        startDateTime,
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        category,
        null,
        null
    );

    switch (status) {
      case PUBLISHED -> event.publish();
      case FINISHED -> event.finish();
      case CANCELLED -> event.cancel();
      case DRAFT -> {
        // Aucun changement : un nouvel événement est DRAFT par défaut.
      }
    }

    return event;
  }

  /**
   * Vérifie qu'un événement ne peut pas être créé
   * avec un nombre maximal de places nul.
   */
  @Test
  void shouldRejectEventCreationWithZeroMaxSeats() {

    LocalDateTime startDateTime = LocalDateTime.now().plusDays(7);

    CreateEventRequest request = new CreateEventRequest(
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        startDateTime.plusHours(2),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        0,
        Category.SPORT,
        10L
    );

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.createEvent(request)
    );

    assertEquals(
        "Le nombre maximal de places doit être strictement supérieur à zéro.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie que l'organisateur propriétaire peut modifier son événement.
   */
  @Test
  void shouldUpdateEventForOwnerOrganizer() throws FunctionalException {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    when(club.getId()).thenReturn(10L);

    authenticateAs(organizer);

    LocalDateTime initialStart = LocalDateTime.now().plusDays(5);
    LocalDateTime updatedStart = LocalDateTime.now().plusDays(10);

    Event event = new Event(
        "Ancien titre",
        "Ancienne description",
        "Montpellier",
        initialStart,
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    UpdateEventRequest request = new UpdateEventRequest(
        "Nouveau titre",
        "Nouvelle description",
        "Nîmes",
        updatedStart,
        null,
        BigDecimal.ZERO,
        BigDecimal.valueOf(20),
        80,
        Category.SPORT
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(eventRepository.save(event))
        .thenReturn(event);

    EventDto result = eventService.updateEvent(100L, request);

    assertEquals("Nouveau titre", result.title());
    assertEquals("Nouvelle description", result.description());
    assertEquals("Nîmes", result.location());
    assertEquals(updatedStart, result.startDateTime());
    assertEquals(BigDecimal.ZERO, result.affiliatedPrice());
    assertEquals(BigDecimal.valueOf(20), result.nonAffiliatedPrice());
    assertEquals(80, result.maxSeats());
    assertEquals(Category.SPORT, result.category());

    verify(eventRepository).save(event);
  }

  /**
   * Vérifie qu'un organisateur ne peut pas modifier
   * un événement appartenant à un autre organisateur.
   */
  @Test
  void shouldRejectUpdateWhenOrganizerIsNotOwner() {

    User owner = mock(User.class);
    User otherOrganizer = mock(User.class);
    Club club = mock(Club.class);

    when(owner.getId()).thenReturn(1L);

    when(otherOrganizer.getId()).thenReturn(2L);
    when(otherOrganizer.getUsername()).thenReturn("other@test.fr");
    when(otherOrganizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(otherOrganizer);

    Event event = new Event(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        owner,
        club
    );

    UpdateEventRequest request = new UpdateEventRequest(
        "Nouveau titre",
        "Nouvelle description",
        "Nîmes",
        LocalDateTime.now().plusDays(10),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        80,
        Category.SPORT
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("other@test.fr"))
        .thenReturn(Optional.of(otherOrganizer));

    assertThrows(
        AccessDeniedException.class,
        () -> eventService.updateEvent(100L, request)
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement terminé ne peut plus être modifié
   * en dehors de ses images.
   */
  @Test
  void shouldRejectUpdateWhenEventIsFinished() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement terminé",
        "Description",
        "Montpellier",
        LocalDateTime.now().minusDays(2),
        LocalDateTime.now().minusDays(1),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    event.finish();

    UpdateEventRequest request = new UpdateEventRequest(
        "Nouveau titre",
        "Nouvelle description",
        "Nîmes",
        LocalDateTime.now().plusDays(10),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        80,
        Category.SPORT
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    assertThrows(
        FunctionalException.class,
        () -> eventService.updateEvent(100L, request)
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie que la date de début d'un événement
   * ne peut pas être modifiée avec une date passée.
   */
  @Test
  void shouldRejectUpdateWithPastStartDate() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    UpdateEventRequest request = new UpdateEventRequest(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().minusDays(1),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.updateEvent(100L, request)
    );

    assertEquals(
        "La date de début doit être postérieure à la date et à l'heure actuelles.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie que la date de fin d'un événement
   * doit rester postérieure à sa date de début.
   */
  @Test
  void shouldRejectUpdateWithInvalidEndDate() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    LocalDateTime updatedStart = LocalDateTime.now().plusDays(10);

    Event event = new Event(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    UpdateEventRequest request = new UpdateEventRequest(
        "Événement",
        "Description",
        "Montpellier",
        updatedStart,
        updatedStart,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.updateEvent(100L, request)
    );

    assertEquals(
        "La date de fin doit être postérieure à la date de début.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement ne peut pas être modifié
   * avec un titre vide.
   */
  @Test
  void shouldRejectUpdateWithoutTitle() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    UpdateEventRequest request = new UpdateEventRequest(
        "   ",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(10),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.updateEvent(100L, request)
    );

    assertEquals(
        "Veuillez renseigner tous les champs obligatoires.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement ne peut pas être modifié
   * avec un nombre maximal de places nul.
   */
  @Test
  void shouldRejectUpdateWithZeroMaxSeats() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    UpdateEventRequest request = new UpdateEventRequest(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(10),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        0,
        Category.CULTURE
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.updateEvent(100L, request)
    );

    assertEquals(
        "Le nombre maximal de places doit être strictement supérieur à zéro.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement ne peut pas être modifié
   * avec un tarif négatif.
   */
  @Test
  void shouldRejectUpdateWithNegativePrice() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    UpdateEventRequest request = new UpdateEventRequest(
        "Événement",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(10),
        null,
        BigDecimal.valueOf(-1),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.updateEvent(100L, request)
    );

    assertEquals(
        "Les tarifs doivent être supérieurs ou égaux à zéro.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie que l'organisateur propriétaire
   * peut publier son événement brouillon.
   */
  @Test
  void shouldPublishDraftEventForOwnerOrganizer() throws FunctionalException {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement brouillon",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(eventRepository.save(event))
        .thenReturn(event);

    EventDto result = eventService.publishEvent(100L);

    assertEquals(EventStatus.PUBLISHED, result.status());

    verify(eventRepository).save(event);
  }

  /**
   * Vérifie qu'un organisateur ne peut pas publier
   * l'événement d'un autre organisateur.
   */
  @Test
  void shouldRejectPublishWhenOrganizerIsNotOwner() {

    User owner = mock(User.class);
    User otherOrganizer = mock(User.class);
    Club club = mock(Club.class);

    when(owner.getId()).thenReturn(1L);

    when(otherOrganizer.getId()).thenReturn(2L);
    when(otherOrganizer.getUsername()).thenReturn("other@test.fr");
    when(otherOrganizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(otherOrganizer);

    Event event = new Event(
        "Événement brouillon",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        owner,
        club
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("other@test.fr"))
        .thenReturn(Optional.of(otherOrganizer));

    ForbiddenException exception = assertThrows(
        ForbiddenException.class,
        () -> eventService.publishEvent(100L)
    );

    assertEquals(
        "Vous n'êtes pas autorisé à publier cet événement.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement qui n'est plus au statut brouillon
   * ne peut pas être publié.
   */
  @Test
  void shouldRejectPublishWhenEventIsNotDraft() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement publié",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    event.publish();

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.publishEvent(100L)
    );

    assertEquals(
        "Cet événement ne peut pas être publié dans son état actuel.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie que l'organisateur propriétaire
   * peut terminer manuellement son événement publié,
   * même lorsqu'aucune date de fin n'est renseignée.
   */
  @Test
  void shouldFinishPublishedEventForOwnerOrganizer()
      throws FunctionalException {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement publié",
        "Description",
        "Montpellier",
        LocalDateTime.now().minusDays(1),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    event.publish();

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(eventRepository.save(event))
        .thenReturn(event);

    EventDto result = eventService.finishEvent(100L);

    assertEquals(EventStatus.FINISHED, result.status());

    verify(eventRepository).save(event);
  }

  /**
   * Vérifie qu'un organisateur ne peut pas terminer
   * l'événement appartenant à un autre organisateur.
   */
  @Test
  void shouldRejectFinishWhenOrganizerIsNotOwner() {

    User owner = mock(User.class);
    User otherOrganizer = mock(User.class);
    Club club = mock(Club.class);

    when(owner.getId()).thenReturn(1L);

    when(otherOrganizer.getId()).thenReturn(2L);
    when(otherOrganizer.getUsername()).thenReturn("other@test.fr");
    when(otherOrganizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(otherOrganizer);

    Event event = new Event(
        "Événement publié",
        "Description",
        "Montpellier",
        LocalDateTime.now().minusDays(1),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        owner,
        club
    );

    event.publish();

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("other@test.fr"))
        .thenReturn(Optional.of(otherOrganizer));

    ForbiddenException exception = assertThrows(
        ForbiddenException.class,
        () -> eventService.finishEvent(100L)
    );

    assertEquals(
        "Vous n’êtes pas autorisé à modifier le statut de cet événement.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement qui n'est pas publié
   * ne peut pas passer au statut terminé.
   */
  @Test
  void shouldRejectFinishWhenEventIsNotPublished() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement brouillon",
        "Description",
        "Montpellier",
        LocalDateTime.now().minusDays(1),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.finishEvent(100L)
    );

    assertEquals(
        "Ce changement de statut n’est pas autorisé.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement sans date de début
   * ne peut pas passer au statut terminé.
   */
  @Test
  void shouldRejectFinishWithoutStartDate() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement publié",
        "Description",
        "Montpellier",
        null,
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    event.publish();

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.finishEvent(100L)
    );

    assertEquals(
        "Ce changement de statut n’est pas autorisé.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement dont la date de fin
   * n'est pas postérieure à la date de début
   * ne peut pas passer au statut terminé.
   */
  @Test
  void shouldRejectFinishWithInvalidEndDate() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    LocalDateTime startDateTime = LocalDateTime.now().minusDays(1);

    Event event = new Event(
        "Événement publié",
        "Description",
        "Montpellier",
        startDateTime,
        startDateTime,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    event.publish();

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.finishEvent(100L)
    );

    assertEquals(
        "Ce changement de statut n’est pas autorisé.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie que l'organisateur propriétaire peut annuler un événement publié
   * possédant au moins un inscrit et que les participants sont informés.
   */
  @Test
  void shouldCancelPublishedEventWithRegisteredUserForOwnerOrganizer()
      throws FunctionalException {

    User organizer = mock(User.class);
    User registeredUser = mock(User.class);
    User waitingUser = mock(User.class);
    Club club = mock(Club.class);

    Registration registeredRegistration = mock(Registration.class);
    Registration waitingRegistration = mock(Registration.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    when(registeredUser.getEmail()).thenReturn("registered@test.fr");
    when(waitingUser.getEmail()).thenReturn("waiting@test.fr");

    when(registeredRegistration.getUser()).thenReturn(registeredUser);
    when(waitingRegistration.getUser()).thenReturn(waitingUser);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement publié",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    event.publish();

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(registrationRepository.countByEventAndStatus(
        event,
        RegistrationStatus.REGISTERED
    )).thenReturn(1L);

    when(registrationRepository.findByEvent(event))
        .thenReturn(List.of(registeredRegistration, waitingRegistration));

    when(eventRepository.save(event))
        .thenReturn(event);

    EventDto result = eventService.cancelEvent(100L);

    assertEquals(EventStatus.CANCELLED, result.status());

    verify(eventRepository).save(event);

    verify(emailService).sendEventCancelledEmail(
        "registered@test.fr",
        "Événement publié"
    );

    verify(emailService).sendEventCancelledEmail(
        "waiting@test.fr",
        "Événement publié"
    );
  }

  /**
   * Vérifie qu'un organisateur ne peut pas annuler
   * l'événement appartenant à un autre organisateur.
   */
  @Test
  void shouldRejectCancelWhenOrganizerIsNotOwner() {

    User owner = mock(User.class);
    User otherOrganizer = mock(User.class);
    Club club = mock(Club.class);

    when(owner.getId()).thenReturn(1L);

    when(otherOrganizer.getId()).thenReturn(2L);
    when(otherOrganizer.getUsername()).thenReturn("other@test.fr");
    when(otherOrganizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(otherOrganizer);

    Event event = new Event(
        "Événement publié",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        owner,
        club
    );

    event.publish();

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("other@test.fr"))
        .thenReturn(Optional.of(otherOrganizer));

    ForbiddenException exception = assertThrows(
        ForbiddenException.class,
        () -> eventService.cancelEvent(100L)
    );

    assertEquals(
        "Vous n’êtes pas autorisé à annuler cet événement.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
  }

  /**
   * Vérifie qu'un événement publié sans inscrit
   * ne peut pas être annulé.
   */
  @Test
  void shouldRejectCancelWhenEventHasNoRegisteredUser() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement publié",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    event.publish();

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    when(registrationRepository.countByEventAndStatus(
        event,
        RegistrationStatus.REGISTERED
    )).thenReturn(0L);

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.cancelEvent(100L)
    );

    assertEquals(
        "Cet événement ne peut pas être annulé dans son état actuel.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
    verify(registrationRepository, never()).findByEvent(event);
  }

  /**
   * Vérifie qu'un événement qui n'est pas publié
   * ne peut pas être annulé.
   */
  @Test
  void shouldRejectCancelWhenEventIsNotPublished() {

    User organizer = mock(User.class);
    Club club = mock(Club.class);

    when(organizer.getId()).thenReturn(1L);
    when(organizer.getUsername()).thenReturn("organizer@test.fr");
    when(organizer.getRole()).thenReturn(Role.ORGANIZER);

    authenticateAs(organizer);

    Event event = new Event(
        "Événement brouillon",
        "Description",
        "Montpellier",
        LocalDateTime.now().plusDays(5),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        50,
        Category.CULTURE,
        organizer,
        club
    );

    when(eventRepository.findById(100L))
        .thenReturn(Optional.of(event));

    when(userRepository.findByEmail("organizer@test.fr"))
        .thenReturn(Optional.of(organizer));

    FunctionalException exception = assertThrows(
        FunctionalException.class,
        () -> eventService.cancelEvent(100L)
    );

    assertEquals(
        "Cet événement ne peut pas être annulé dans son état actuel.",
        exception.getMessage()
    );

    verify(eventRepository, never()).save(any(Event.class));
    verify(registrationRepository, never())
        .countByEventAndStatus(
            event,
            RegistrationStatus.REGISTERED
        );
  }
}
