package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.EventCardDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Génère l'export Excel du calendrier personnel des utilisateurs (CAL-02).
 */
@Service
public class CalendarExcelService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CalendarService calendarService;

    /**
     * Initialise le service de génération Excel.
     *
     * @param calendarService service fournissant les événements du calendrier
     */
    public CalendarExcelService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * Génère le classeur Excel du calendrier de l'utilisateur courant, sur la période donnée.
     *
     * @param from début de la période
     * @param to   fin de la période
     * @return contenu du fichier .xlsx généré
     */
    @Transactional(readOnly = true)
    public byte[] generateExcel(LocalDate from, LocalDate to) {

        List<EventCardDto> events = calendarService.getMyCalendar(from, to);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Mon calendrier");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Titre");
            header.createCell(1).setCellValue("Lieu");
            header.createCell(2).setCellValue("Début");
            header.createCell(3).setCellValue("Fin");
            header.createCell(4).setCellValue("Catégorie");

            int rowIndex = 1;
            for (EventCardDto event : events) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(event.title());
                row.createCell(1).setCellValue(event.location());
                row.createCell(2).setCellValue(formatDateTime(event.startDateTime()));
                row.createCell(3).setCellValue(formatDateTime(event.endDateTime()));
                row.createCell(4).setCellValue(event.category().name());
            }

            for (int col = 0; col < 5; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Échec de la génération du fichier Excel du calendrier", e
            );
        }
    }

    /**
     * Formate une date et une heure pour l'export Excel.
     *
     * @param dateTime date et heure à formater
     * @return date formatée, ou chaîne vide si absente
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }
}