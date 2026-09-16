package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.dto.CreateEventRequest;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.Category;
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
}