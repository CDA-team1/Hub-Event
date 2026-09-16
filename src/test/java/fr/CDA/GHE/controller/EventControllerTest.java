package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.dto.CreateEventRequest;
import fr.CDA.GHE.dto.UpdateEventRequest;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.exception.ForbiddenException;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


/**
 * Tests du contrôleur des événements.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EventControllerTest {

  /**
   * Client simulé permettant d'exécuter des requêtes HTTP
   * sur l'application pendant les tests.
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * Service des événements remplacé par un mock
   * dans le contexte Spring de test.
   */
  @MockitoBean
  private EventService eventService;

  /**
   * Vérifie que la route publique GET /events retourne
   * les listes d'événements fournies par le service.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldReturnPublicEventsWithoutAuthentication() throws Exception {

    EventCardDto cultureEvent = new EventCardDto(
        1L,
        "Exposition",
        "Montpellier",
        LocalDateTime.of(2026, 10, 15, 10, 0),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        Category.CULTURE
    );

    EventListDto eventListDto = new EventListDto(
        List.of(cultureEvent),
        List.of(),
        List.of(),
        List.of()
    );

    when(eventService.getPublicEvents())
        .thenReturn(eventListDto);

    mockMvc.perform(get("/events"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.cultureEvents.length()").value(1))
        .andExpect(jsonPath("$.cultureEvents[0].id").value(1))
        .andExpect(jsonPath("$.cultureEvents[0].title").value("Exposition"))
        .andExpect(jsonPath("$.cultureEvents[0].location").value("Montpellier"))
        .andExpect(jsonPath("$.cultureEvents[0].category").value("CULTURE"))
        .andExpect(jsonPath("$.leisureEvents").isEmpty())
        .andExpect(jsonPath("$.sportEvents").isEmpty())
        .andExpect(jsonPath("$.pastEvents").isEmpty());

    verify(eventService).getPublicEvents();
  }

  /**
   * Vérifie que la route publique GET /events/{id}
   * retourne le détail de l'événement fourni par le service.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldReturnEventDetailWithoutAuthentication() throws Exception {

    EventDetailResponse eventDetail = new EventDetailResponse(
        "Concert",
        "Description de test",
        "Montpellier",
        LocalDateTime.of(2026, 11, 20, 20, 0),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100
    );

    when(eventService.getEventDetail(1L))
        .thenReturn(eventDetail);

    mockMvc.perform(get("/events/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Concert"))
        .andExpect(jsonPath("$.description").value("Description de test"))
        .andExpect(jsonPath("$.location").value("Montpellier"))
        .andExpect(jsonPath("$.affiliatedPrice").value(10))
        .andExpect(jsonPath("$.nonAffiliatedPrice").value(15))
        .andExpect(jsonPath("$.maxSeats").value(100));

    verify(eventService).getEventDetail(1L);
  }

  /**
   * Vérifie que la route GET /events/{id} retourne 404
   * lorsque l'événement n'existe pas ou n'est pas accessible.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldReturnNotFoundWhenEventDetailIsNotAccessible() throws Exception {

    when(eventService.getEventDetail(999L))
        .thenThrow(new NotFoundException("Événement introuvable"));

    mockMvc.perform(get("/events/999"))
        .andExpect(status().isNotFound());

    verify(eventService).getEventDetail(999L);
  }

  /**
   * Vérifie qu'un organisateur authentifié peut créer un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldCreateEventAsOrganizer() throws Exception {
    LocalDateTime startDateTime =
        LocalDateTime.of(2026, 11, 20, 18, 0);

    LocalDateTime endDateTime =
        LocalDateTime.of(2026, 11, 20, 20, 0);

    EventDto createdEvent = new EventDto(
        1L,
        "Tournoi de tennis",
        "Tournoi ouvert aux membres",
        "Montpellier",
        startDateTime,
        endDateTime,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        EventStatus.DRAFT,
        Category.SPORT,
        1L,
        10L
    );

    when(eventService.createEvent(any(CreateEventRequest.class)))
        .thenReturn(createdEvent);

    String requestBody = """
        {
          "title": "Tournoi de tennis",
          "description": "Tournoi ouvert aux membres",
          "location": "Montpellier",
          "startDateTime": "2026-11-20T18:00:00",
          "endDateTime": "2026-11-20T20:00:00",
          "affiliatedPrice": 10,
          "nonAffiliatedPrice": 15,
          "maxSeats": 100,
          "category": "SPORT",
          "clubId": 10
        }
        """;

    mockMvc.perform(post("/events")
            .with(user("organizer@test.fr").roles("ORGANIZER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Tournoi de tennis"))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.category").value("SPORT"))
        .andExpect(jsonPath("$.organizerId").value(1))
        .andExpect(jsonPath("$.clubId").value(10));

    verify(eventService).createEvent(any(CreateEventRequest.class));
  }

  /**
   * Vérifie qu'un utilisateur non authentifié
   * ne peut pas créer un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventCreationWithoutAuthentication() throws Exception {

    String requestBody = """
        {
          "title": "Tournoi de tennis",
          "description": "Tournoi ouvert aux membres",
          "location": "Montpellier",
          "startDateTime": "2026-11-20T18:00:00",
          "endDateTime": "2026-11-20T20:00:00",
          "affiliatedPrice": 10,
          "nonAffiliatedPrice": 15,
          "maxSeats": 100,
          "category": "SPORT",
          "clubId": 10
        }
        """;

    mockMvc.perform(post("/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isUnauthorized());

    verify(eventService, never())
        .createEvent(any(CreateEventRequest.class));
  }

  /**
   * Vérifie qu'un membre authentifié
   * ne peut pas créer un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventCreationForMember() throws Exception {

    String requestBody = """
        {
          "title": "Tournoi de tennis",
          "description": "Tournoi ouvert aux membres",
          "location": "Montpellier",
          "startDateTime": "2026-11-20T18:00:00",
          "endDateTime": "2026-11-20T20:00:00",
          "affiliatedPrice": 10,
          "nonAffiliatedPrice": 15,
          "maxSeats": 100,
          "category": "SPORT",
          "clubId": 10
        }
        """;

    mockMvc.perform(post("/events")
            .with(user("member@test.fr").roles("MEMBER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isForbidden());

    verify(eventService, never())
        .createEvent(any(CreateEventRequest.class));
  }

  /**
   * Vérifie qu'un organisateur authentifié
   * peut modifier un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldUpdateEventAsOrganizer() throws Exception {

    LocalDateTime startDateTime =
        LocalDateTime.of(2026, 11, 25, 18, 0);

    LocalDateTime endDateTime =
        LocalDateTime.of(2026, 11, 25, 20, 0);

    EventDto updatedEvent = new EventDto(
        100L,
        "Nouveau titre",
        "Nouvelle description",
        "Nîmes",
        startDateTime,
        endDateTime,
        BigDecimal.ZERO,
        BigDecimal.valueOf(20),
        80,
        EventStatus.DRAFT,
        Category.SPORT,
        1L,
        10L
    );

    when(eventService.updateEvent(
        eq(100L),
        any(UpdateEventRequest.class)
    )).thenReturn(updatedEvent);

    String requestBody = """
        {
          "title": "Nouveau titre",
          "description": "Nouvelle description",
          "location": "Nîmes",
          "startDateTime": "2026-11-25T18:00:00",
          "endDateTime": "2026-11-25T20:00:00",
          "affiliatedPrice": 0,
          "nonAffiliatedPrice": 20,
          "maxSeats": 80,
          "category": "SPORT"
        }
        """;

    mockMvc.perform(put("/events/100")
            .with(user("organizer@test.fr").roles("ORGANIZER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.title").value("Nouveau titre"))
        .andExpect(jsonPath("$.location").value("Nîmes"))
        .andExpect(jsonPath("$.maxSeats").value(80))
        .andExpect(jsonPath("$.category").value("SPORT"));

    verify(eventService).updateEvent(
        eq(100L),
        any(UpdateEventRequest.class)
    );
  }

  /**
   * Vérifie qu'un membre authentifié
   * ne peut pas modifier un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventUpdateForMember() throws Exception {

    String requestBody = """
        {
          "title": "Nouveau titre",
          "description": "Nouvelle description",
          "location": "Nîmes",
          "startDateTime": "2026-11-25T18:00:00",
          "endDateTime": "2026-11-25T20:00:00",
          "affiliatedPrice": 0,
          "nonAffiliatedPrice": 20,
          "maxSeats": 80,
          "category": "SPORT"
        }
        """;

    mockMvc.perform(put("/events/100")
            .with(user("member@test.fr").roles("MEMBER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isForbidden());

    verify(eventService, never())
        .updateEvent(
            eq(100L),
            any(UpdateEventRequest.class)
        );
  }

  /**
   * Vérifie qu'un utilisateur non authentifié
   * ne peut pas modifier un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventUpdateWithoutAuthentication() throws Exception {

    String requestBody = """
        {
          "title": "Nouveau titre",
          "description": "Nouvelle description",
          "location": "Nîmes",
          "startDateTime": "2026-11-25T18:00:00",
          "endDateTime": "2026-11-25T20:00:00",
          "affiliatedPrice": 0,
          "nonAffiliatedPrice": 20,
          "maxSeats": 80,
          "category": "SPORT"
        }
        """;

    mockMvc.perform(put("/events/100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isUnauthorized());

    verify(eventService, never())
        .updateEvent(
            eq(100L),
            any(UpdateEventRequest.class)
        );
  }

  /**
   * Vérifie qu'un organisateur authentifié
   * peut publier un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldPublishEventAsOrganizer() throws Exception {

    EventDto publishedEvent = new EventDto(
        100L,
        "Concert",
        "Description",
        "Montpellier",
        LocalDateTime.of(2026, 11, 20, 20, 0),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        EventStatus.PUBLISHED,
        Category.CULTURE,
        1L,
        10L
    );

    when(eventService.publishEvent(100L))
        .thenReturn(publishedEvent);

    mockMvc.perform(post("/events/100/publish")
            .with(user("organizer@test.fr").roles("ORGANIZER")))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.title").value("Concert"))
        .andExpect(jsonPath("$.status").value("PUBLISHED"));

    verify(eventService).publishEvent(100L);
  }

  /**
   * Vérifie qu'un membre authentifié
   * ne peut pas publier un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventPublicationForMember() throws Exception {

    mockMvc.perform(post("/events/100/publish")
            .with(user("member@test.fr").roles("MEMBER")))
        .andExpect(status().isForbidden());

    verify(eventService, never()).publishEvent(100L);
  }

  /**
   * Vérifie qu'un utilisateur non authentifié
   * ne peut pas publier un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventPublicationWithoutAuthentication() throws Exception {

    mockMvc.perform(post("/events/100/publish"))
        .andExpect(status().isUnauthorized());

    verify(eventService, never()).publishEvent(100L);
  }

  /**
   * Vérifie qu'un organisateur authentifié mais non propriétaire
   * reçoit une réponse 403 lors de la publication.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventPublicationForNonOwnerOrganizer() throws Exception {

    when(eventService.publishEvent(100L))
        .thenThrow(new ForbiddenException(
            "Vous n'êtes pas autorisé à publier cet événement."
        ));

    mockMvc.perform(post("/events/100/publish")
            .with(user("organizer@test.fr").roles("ORGANIZER")))
        .andExpect(status().isForbidden());

    verify(eventService).publishEvent(100L);
  }

  /**
   * Vérifie qu'un événement qui n'est pas au statut brouillon
   * ne peut pas être publié.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventPublicationWhenEventIsNotDraft() throws Exception {

    when(eventService.publishEvent(100L))
        .thenThrow(new FunctionalException(
            "Cet événement ne peut pas être publié dans son état actuel."
        ));

    mockMvc.perform(post("/events/100/publish")
            .with(user("organizer@test.fr").roles("ORGANIZER")))
        .andExpect(status().isBadRequest());

    verify(eventService).publishEvent(100L);
  }

  /**
   * Vérifie qu'un organisateur authentifié
   * peut terminer un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldFinishEventAsOrganizer() throws Exception {

    EventDto finishedEvent = new EventDto(
        100L,
        "Concert",
        "Description",
        "Montpellier",
        LocalDateTime.of(2026, 9, 15, 20, 0),
        null,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        EventStatus.FINISHED,
        Category.CULTURE,
        1L,
        10L
    );

    when(eventService.finishEvent(100L))
        .thenReturn(finishedEvent);

    mockMvc.perform(post("/events/100/status")
            .with(user("organizer@test.fr").roles("ORGANIZER")))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.title").value("Concert"))
        .andExpect(jsonPath("$.status").value("FINISHED"));

    verify(eventService).finishEvent(100L);
  }

  /**
   * Vérifie qu'un membre authentifié
   * ne peut pas terminer un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventFinishForMember() throws Exception {

    mockMvc.perform(post("/events/100/status")
            .with(user("member@test.fr").roles("MEMBER")))
        .andExpect(status().isForbidden());

    verify(eventService, never()).finishEvent(100L);
  }

  /**
   * Vérifie qu'un utilisateur non authentifié
   * ne peut pas terminer un événement.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventFinishWithoutAuthentication() throws Exception {

    mockMvc.perform(post("/events/100/status"))
        .andExpect(status().isUnauthorized());

    verify(eventService, never()).finishEvent(100L);
  }

  /**
   * Vérifie qu'un organisateur authentifié mais non propriétaire
   * reçoit une réponse 403 lors du changement de statut.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventFinishForNonOwnerOrganizer() throws Exception {

    when(eventService.finishEvent(100L))
        .thenThrow(new ForbiddenException(
            "Vous n’êtes pas autorisé à modifier le statut de cet événement."
        ));

    mockMvc.perform(post("/events/100/status")
            .with(user("organizer@test.fr").roles("ORGANIZER")))
        .andExpect(status().isForbidden());

    verify(eventService).finishEvent(100L);
  }

  /**
   * Vérifie qu'un changement de statut non autorisé
   * retourne une réponse 400.
   *
   * @throws Exception si l'exécution de la requête HTTP échoue
   */
  @Test
  void shouldRejectEventFinishWhenStatusChangeIsNotAllowed()
      throws Exception {

    when(eventService.finishEvent(100L))
        .thenThrow(new FunctionalException(
            "Ce changement de statut n’est pas autorisé."
        ));

    mockMvc.perform(post("/events/100/status")
            .with(user("organizer@test.fr").roles("ORGANIZER")))
        .andExpect(status().isBadRequest());

    verify(eventService).finishEvent(100L);
  }
}