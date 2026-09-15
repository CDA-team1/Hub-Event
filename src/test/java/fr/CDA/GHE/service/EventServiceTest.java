package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.repository.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
   * Nettoie le contexte de sécurité après chaque test
   * afin qu'une authentification ne perturbe pas le test suivant.
   */
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
}
