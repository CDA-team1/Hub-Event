package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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