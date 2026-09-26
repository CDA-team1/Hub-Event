package fr.CDA.GHE.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import fr.CDA.GHE.dto.EventDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Génère la fiche détaillée d'un événement au format PDF.
 */
@Service

public class EventPdfService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final EventService eventService;

  /**
   * Initialise le service de génération PDF.
   *
   * @param eventService service permettant de récupérer
   *                     le détail accessible d'un événement
   */
  public EventPdfService(EventService eventService) {
    this.eventService = eventService;
  }

  /**
   * Génère la fiche détaillée d'un événement au format PDF.
   *
   * @param eventId identifiant de l'événement
   * @return contenu du fichier PDF généré
   */
  @Transactional(readOnly = true)
  public byte[] generatePdf(Long eventId) {

    EventDetailResponse event = eventService.getEventDetail(eventId);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (PdfWriter writer = new PdfWriter(outputStream);
         PdfDocument pdfDocument = new PdfDocument(writer);
         Document document = new Document(pdfDocument)) {

      document.add(
          new Paragraph("Fiche de l'événement")
              .setFontSize(18)
      );

      document.add(
          new Paragraph(event.title())
              .setFontSize(14)
      );

      document.add(
          new Paragraph("Description : " + event.description())
      );

      document.add(
          new Paragraph("Lieu : " + event.location())
      );

      document.add(
          new Paragraph(
              "Début : " + formatDateTime(event.startDateTime())
          )
      );

      document.add(
          new Paragraph(
              "Fin : " + formatDateTime(event.endDateTime())
          )
      );

      document.add(
          new Paragraph(
              "Tarif affilié : " + formatPrice(event.affiliatedPrice())
          )
      );

      document.add(
          new Paragraph(
              "Tarif non affilié : "
                  + formatPrice(event.nonAffiliatedPrice())
          )
      );

      document.add(
          new Paragraph(
              "Nombre maximal de places : " + event.maxSeats()
          )
      );

    } catch (IOException e) {
      throw new IllegalStateException(
          "Échec de la génération du PDF de l'événement",
          e
      );
    }

    return outputStream.toByteArray();
  }

  /**
   * Formate une date et une heure pour le PDF.
   *
   * @param dateTime date et heure à formater
   * @return date formatée ou mention d'absence
   */
  private String formatDateTime(LocalDateTime dateTime) {
    return dateTime == null
        ? "Non renseignée"
        : dateTime.format(DATE_TIME_FORMATTER);
  }

  /**
   * Formate un tarif pour le PDF.
   *
   * @param price tarif à formater
   * @return tarif formaté en euros
   */
  private String formatPrice(BigDecimal price) {
    return price == null
        ? "Non renseigné"
        : price.toPlainString() + " €";
  }

}
