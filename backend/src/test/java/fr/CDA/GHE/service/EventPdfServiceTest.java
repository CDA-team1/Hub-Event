package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service de génération des fiches événement
 * au format PDF.
 */
@ExtendWith(MockitoExtension.class)
public class EventPdfServiceTest {

  /**
   * Service des événements simulé pour fournir le détail
   * de l'événement à exporter.
   */
  @Mock
  private EventService eventService;

  /**
   * Service de génération PDF testé.
   */
  @InjectMocks
  private EventPdfService eventPdfService;

  /**
   * Vérifie qu'un PDF valide est généré à partir
   * du détail d'un événement accessible.
   */
  @Test
  void shouldGenerateEventPdf() {

    EventDetailResponse eventDetail = new EventDetailResponse(
        "Tournoi de badminton",
        "Tournoi ouvert à tous",
        "Montpellier",
        LocalDateTime.of(2026, 11, 20, 18, 0),
        LocalDateTime.of(2026, 11, 20, 21, 0),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
            List.of()
    );

    when(eventService.getEventDetail(1L))
        .thenReturn(eventDetail);

    byte[] pdf = eventPdfService.generatePdf(1L);

    assertNotNull(pdf);
    assertTrue(pdf.length > 4);

    String pdfHeader = new String(
        pdf,
        0,
        4,
        StandardCharsets.US_ASCII
    );

    assertEquals("%PDF", pdfHeader);

    verify(eventService).getEventDetail(1L);
  }

  /**
   * Vérifie que l'erreur de consultation est propagée
   * lorsqu'un événement n'existe pas ou n'est pas accessible.
   */
  @Test
  void shouldPropagateNotFoundWhenEventIsNotAccessible() {

    when(eventService.getEventDetail(999L))
        .thenThrow(new NotFoundException("Événement introuvable"));

    assertThrows(
        NotFoundException.class,
        () -> eventPdfService.generatePdf(999L)
    );

    verify(eventService).getEventDetail(999L);
  }
}
