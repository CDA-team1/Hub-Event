package fr.CDA.GHE.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.UnitValue;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Génère la fiche détaillée d'un événement au format PDF,
 * avec le même visuel que l'application.
 */
@Service

public class EventPdfService {

  private static final String APP_NAME = "Hub évènementiel";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");

  // Palette de l'application (styles.css), pour que la fiche garde son visuel.
  private static final DeviceRgb TEXT_COLOR = new DeviceRgb(0x1e, 0x29, 0x3b);
  private static final DeviceRgb MUTED_COLOR = new DeviceRgb(0x64, 0x74, 0x8b);
  private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(0x64, 0x74, 0x8b);
  private static final DeviceRgb PRIMARY_DARK_COLOR = new DeviceRgb(0x47, 0x55, 0x69);
  private static final DeviceRgb BORDER_COLOR = new DeviceRgb(0xe2, 0xe8, 0xf0);
  private static final DeviceRgb SURFACE_COLOR = new DeviceRgb(0xf8, 0xfa, 0xfc);

  private static final float PAGE_MARGIN = 40;
  private static final float HEADER_HEIGHT = 64;
  private static final float FOOTER_HEIGHT = 50;

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
         Document document = new Document(pdfDocument, PageSize.A4)) {

      PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
      PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

      pdfDocument.getDocumentInfo().setTitle(event.title());
      pdfDocument.getDocumentInfo().setAuthor(APP_NAME);
      pdfDocument.addEventHandler(
          PdfDocumentEvent.END_PAGE,
          new PageDecorator(regularFont, boldFont)
      );

      document.setMargins(
          HEADER_HEIGHT + PAGE_MARGIN,
          PAGE_MARGIN,
          FOOTER_HEIGHT + PAGE_MARGIN / 2,
          PAGE_MARGIN
      );
      document.setFont(regularFont);
      document.setFontColor(TEXT_COLOR);

      document.add(buildOverline(event, boldFont));
      document.add(
          new Paragraph(event.title())
              .setFont(boldFont)
              .setFontSize(24)
              .setMarginTop(0)
              .setMarginBottom(18)
      );
      document.add(buildFacts(event, boldFont));
      document.add(
          new Paragraph("DESCRIPTION")
              .setFont(boldFont)
              .setFontSize(10)
              .setCharacterSpacing(1.2f)
              .setFontColor(PRIMARY_DARK_COLOR)
              .setMarginTop(26)
              .setMarginBottom(8)
      );
      document.add(
          new Paragraph(formatDescription(event.description()))
              .setFontSize(11)
              .setMultipliedLeading(1.5f)
              .setMarginTop(0)
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
   * Construit la ligne au-dessus du titre : la catégorie de l'événement
   * et, s'il n'est pas publié, son statut.
   *
   * @param event    détail de l'événement
   * @param boldFont police utilisée pour la catégorie et le statut
   * @return paragraphe de la catégorie et du statut
   */
  private Paragraph buildOverline(EventDetailResponse event, PdfFont boldFont) {

    Paragraph overline = new Paragraph()
        .setFont(boldFont)
        .setFontSize(9)
        .setMarginTop(0)
        .setMarginBottom(6);

    overline.add(
        new Text(categoryLabel(event.category()).toUpperCase())
            .setCharacterSpacing(1.2f)
            .setFontColor(MUTED_COLOR)
    );

    if (event.status() != null && event.status() != EventStatus.PUBLISHED) {
      DeviceRgb statusColor = statusColor(event.status());
      overline.add(new Text("   "));
      overline.add(
          new Text(statusLabel(event.status()))
              .setFontColor(statusColor)
              .setBackgroundColor(tint(statusColor), 5, 5, 3, 3)
      );
    }

    return overline;
  }

  /**
   * Construit le tableau des informations pratiques sur deux colonnes
   * (dates, lieu, tarifs et places), comme dans la page de l'événement.
   *
   * @param event    détail de l'événement
   * @param boldFont police utilisée pour les valeurs
   * @return tableau des informations de l'événement
   */
  private Table buildFacts(EventDetailResponse event, PdfFont boldFont) {

    List<String[]> facts = new ArrayList<>();
    facts.add(new String[] {"Début", formatDateTime(event.startDateTime())});
    if (event.endDateTime() != null) {
      facts.add(new String[] {"Fin", formatDateTime(event.endDateTime())});
    }
    facts.add(new String[] {"Lieu", valueOrDefault(event.location())});
    facts.add(new String[] {"Tarif affilié", formatPrice(event.affiliatedPrice())});
    facts.add(
        new String[] {"Tarif non affilié", formatPrice(event.nonAffiliatedPrice())}
    );
    facts.add(
        new String[] {
            "Places",
            event.maxSeats() == null ? "Non renseigné" : event.maxSeats().toString()
        }
    );

    Table table = new Table(UnitValue.createPercentArray(new float[] {1, 1}))
        .useAllAvailableWidth()
        .setBorderTop(new SolidBorder(BORDER_COLOR, 0.75f));

    for (String[] fact : facts) {
      table.addCell(buildFactCell(fact[0], fact[1], boldFont));
    }
    // Complète la dernière ligne pour que le tableau garde ses deux colonnes.
    if (facts.size() % 2 == 1) {
      table.addCell(
          new Cell().setBorder(Border.NO_BORDER)
              .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.75f))
      );
    }

    return table;
  }

  /**
   * Construit une cellule d'information : un libellé discret au-dessus
   * d'une valeur en gras.
   *
   * @param label    libellé de l'information
   * @param value    valeur de l'information
   * @param boldFont police de la valeur
   * @return cellule du tableau des informations
   */
  private Cell buildFactCell(String label, String value, PdfFont boldFont) {

    return new Cell()
        .setBorder(Border.NO_BORDER)
        .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.75f))
        .setPaddingTop(10)
        .setPaddingBottom(10)
        .setPaddingLeft(0)
        .add(
            new Paragraph(label)
                .setFontSize(9)
                .setFontColor(MUTED_COLOR)
                .setMargin(0)
        )
        .add(
            new Paragraph(value)
                .setFont(boldFont)
                .setFontSize(12)
                .setMarginTop(3)
                .setMarginBottom(0)
        );
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

  /**
   * Renvoie la valeur, ou une mention d'absence si elle est vide.
   *
   * @param value valeur à afficher
   * @return valeur ou mention d'absence
   */
  private String valueOrDefault(String value) {
    return value == null || value.isBlank() ? "Non renseigné" : value;
  }

  /**
   * Prépare la description pour le PDF.
   *
   * @param description description de l'événement
   * @return description ou mention d'absence
   */
  private String formatDescription(String description) {
    return description == null || description.isBlank()
        ? "Aucune description."
        : description;
  }

  /**
   * Donne le libellé français d'une catégorie.
   *
   * @param category catégorie de l'événement
   * @return libellé de la catégorie
   */
  private String categoryLabel(Category category) {
    if (category == null) {
      return "Évènement";
    }
    return switch (category) {
      case CULTURE -> "Culture";
      case LEISURE -> "Loisirs";
      case SPORT -> "Sport";
    };
  }

  /**
   * Donne le libellé français d'un statut.
   *
   * @param status statut de l'événement
   * @return libellé du statut
   */
  private String statusLabel(EventStatus status) {
    return switch (status) {
      case DRAFT -> "Brouillon";
      case PUBLISHED -> "Publié";
      case CANCELLED -> "Annulé";
      case FINISHED -> "Terminé";
    };
  }

  /**
   * Donne la couleur d'un statut, la même que le badge de l'application.
   *
   * @param status statut de l'événement
   * @return couleur du texte du statut
   */
  private DeviceRgb statusColor(EventStatus status) {
    return switch (status) {
      case DRAFT -> new DeviceRgb(0x6b, 0x72, 0x80);
      case PUBLISHED -> new DeviceRgb(0x2e, 0x9c, 0x55);
      case CANCELLED -> new DeviceRgb(0xd3, 0x3f, 0x3f);
      case FINISHED -> new DeviceRgb(0x63, 0x66, 0xa8);
    };
  }

  /**
   * Éclaircit une couleur à 12 % sur fond blanc pour le fond d'un badge.
   *
   * @param color couleur du texte du badge
   * @return couleur de fond du badge
   */
  private DeviceRgb tint(DeviceRgb color) {
    float[] rgb = color.getColorValue();
    return new DeviceRgb(
        rgb[0] * 0.12f + 0.88f,
        rgb[1] * 0.12f + 0.88f,
        rgb[2] * 0.12f + 0.88f
    );
  }

  /**
   * Dessine sur chaque page l'en-tête avec le nom de l'application
   * et le pied de page avec la date de génération et le numéro de page.
   */
  private static final class PageDecorator extends AbstractPdfDocumentEventHandler {

    private final PdfFont regularFont;
    private final PdfFont boldFont;
    private final String generatedOn = LocalDate.now().format(DATE_FORMATTER);

    private PageDecorator(PdfFont regularFont, PdfFont boldFont) {
      this.regularFont = regularFont;
      this.boldFont = boldFont;
    }

    @Override
    protected void onAcceptedEvent(AbstractPdfDocumentEvent event) {
      PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
      PdfDocument pdfDocument = documentEvent.getDocument();
      PdfPage page = documentEvent.getPage();
      Rectangle pageSize = page.getPageSize();

      PdfCanvas canvas = new PdfCanvas(
          page.newContentStreamBefore(),
          page.getResources(),
          pdfDocument
      );
      drawHeader(canvas, pageSize);
      drawFooter(canvas, pageSize, pdfDocument.getPageNumber(page));
      canvas.release();
    }

    private void drawHeader(PdfCanvas canvas, Rectangle pageSize) {
      float bottom = pageSize.getTop() - HEADER_HEIGHT;
      float middle = bottom + HEADER_HEIGHT / 2;
      float logoSize = 22;

      canvas.saveState()
          .setFillColor(SURFACE_COLOR)
          .rectangle(0, bottom, pageSize.getWidth(), HEADER_HEIGHT)
          .fill()
          .setStrokeColor(BORDER_COLOR)
          .setLineWidth(0.75f)
          .moveTo(0, bottom)
          .lineTo(pageSize.getWidth(), bottom)
          .stroke()
          .setFillColor(PRIMARY_COLOR)
          .roundRectangle(PAGE_MARGIN, middle - logoSize / 2, logoSize, logoSize, 5)
          .fill()
          .beginText()
          .setFontAndSize(boldFont, 15)
          .setFillColor(PRIMARY_DARK_COLOR)
          .moveText(PAGE_MARGIN + logoSize + 10, middle - 5)
          .showText(APP_NAME)
          .endText()
          .restoreState();
    }

    private void drawFooter(PdfCanvas canvas, Rectangle pageSize, int pageNumber) {
      float lineY = FOOTER_HEIGHT;
      float textY = lineY - 18;
      String pageLabel = "Page " + pageNumber;
      float pageLabelWidth = regularFont.getWidth(pageLabel, 9);

      canvas.saveState()
          .setStrokeColor(BORDER_COLOR)
          .setLineWidth(0.75f)
          .moveTo(PAGE_MARGIN, lineY)
          .lineTo(pageSize.getWidth() - PAGE_MARGIN, lineY)
          .stroke()
          .beginText()
          .setFontAndSize(regularFont, 9)
          .setFillColor(MUTED_COLOR)
          .moveText(PAGE_MARGIN, textY)
          .showText(APP_NAME + " · Fiche générée le " + generatedOn)
          .endText()
          .beginText()
          .setFontAndSize(regularFont, 9)
          .setFillColor(MUTED_COLOR)
          .moveText(pageSize.getWidth() - PAGE_MARGIN - pageLabelWidth, textY)
          .showText(pageLabel)
          .endText()
          .restoreState();
    }
  }

}
