package fr.CDA.GHE.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
            Category.SPORT,
            EventStatus.PUBLISHED,
            60,
            0,
            false,
            null,
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

  /**
   * Vérifie que le PDF affiche le nom de l'application en en-tête
   * ainsi que les informations de l'événement.
   */
  @Test
  void shouldShowAppNameAndEventInformationInPdf() throws IOException {

    when(eventService.getEventDetail(1L)).thenReturn(
        eventDetail(
            "Tournoi ouvert à tous",
            LocalDateTime.of(2026, 11, 20, 21, 0),
            EventStatus.PUBLISHED
        )
    );

    String text = extractText(eventPdfService.generatePdf(1L));

    assertTrue(text.contains("Hub évènementiel"));
    assertTrue(text.contains("Tournoi de badminton"));
    assertTrue(text.contains("Tournoi ouvert à tous"));
    assertTrue(text.contains("Montpellier"));
    assertTrue(text.contains("20/11/2026 18:00"));
    assertTrue(text.contains("20/11/2026 21:00"));
    assertTrue(text.contains("10 €"));
    assertTrue(text.contains("15 €"));
  }

  /**
   * Vérifie que le statut n'est affiché que pour un événement
   * qui n'est pas publié, comme dans l'application.
   */
  @Test
  void shouldShowStatusOnlyWhenEventIsNotPublished() throws IOException {

    when(eventService.getEventDetail(1L)).thenReturn(
        eventDetail("Description", null, EventStatus.CANCELLED)
    );
    when(eventService.getEventDetail(2L)).thenReturn(
        eventDetail("Description", null, EventStatus.PUBLISHED)
    );

    assertTrue(extractText(eventPdfService.generatePdf(1L)).contains("Annulé"));
    assertFalse(extractText(eventPdfService.generatePdf(2L)).contains("Publié"));
  }

  /**
   * Vérifie que la date de fin et la description absentes
   * ne cassent pas la génération.
   */
  @Test
  void shouldHandleMissingEndDateAndDescription() throws IOException {

    when(eventService.getEventDetail(1L)).thenReturn(
        eventDetail(null, null, EventStatus.PUBLISHED)
    );

    String text = extractText(eventPdfService.generatePdf(1L));

    assertFalse(text.contains("Fin"));
    assertTrue(text.contains("Aucune description."));
  }

  /**
   * Construit le détail d'un événement de test.
   *
   * @param description description de l'événement
   * @param endDateTime date de fin de l'événement
   * @param status      statut de l'événement
   * @return détail de l'événement
   */
  private EventDetailResponse eventDetail(
      String description,
      LocalDateTime endDateTime,
      EventStatus status
  ) {
    return new EventDetailResponse(
        "Tournoi de badminton",
        description,
        "Montpellier",
        LocalDateTime.of(2026, 11, 20, 18, 0),
        endDateTime,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(15),
        100,
        Category.SPORT,
        status,
        60,
        0,
        false,
        null,
        List.of()
    );
  }

  /**
   * Extrait le texte de la première page d'un PDF.
   *
   * @param pdf contenu du fichier PDF
   * @return texte de la première page
   * @throws IOException si le PDF ne peut pas être lu
   */
  private String extractText(byte[] pdf) throws IOException {
    try (PdfDocument document =
             new PdfDocument(new PdfReader(new ByteArrayInputStream(pdf)))) {
      return PdfTextExtractor.getTextFromPage(document.getPage(1));
    }
  }
}
