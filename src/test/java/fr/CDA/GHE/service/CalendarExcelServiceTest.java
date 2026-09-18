package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.entity.enums.Category;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests de la génération Excel du calendrier (CAL-02).
 */
@SpringBootTest
@ActiveProfiles("test")
class CalendarExcelServiceTest {

    @Autowired
    private CalendarExcelService calendarExcelService;

    @MockitoBean
    private CalendarService calendarService;

    /**
     * Vérifie que chaque événement retourné par le service calendrier
     * produit bien une ligne dans le fichier Excel généré.
     */
    @Test
    void shouldGenerateExcelWithOneRowPerEvent() throws IOException {

        EventCardDto event = new EventCardDto(
                1L, "Tournoi de tennis", "Lyon",
                LocalDateTime.of(2026, 10, 15, 14, 0),
                LocalDateTime.of(2026, 10, 15, 18, 0),
                BigDecimal.valueOf(5), BigDecimal.valueOf(10), Category.SPORT
        );

        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        when(calendarService.getMyCalendar(from, to)).thenReturn(List.of(event));

        byte[] excel = calendarExcelService.generateExcel(from, to);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excel))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row header = sheet.getRow(0);
            assertEquals("Titre", header.getCell(0).getStringCellValue());

            Row dataRow = sheet.getRow(1);
            assertEquals("Tournoi de tennis", dataRow.getCell(0).getStringCellValue());
            assertEquals("Lyon", dataRow.getCell(1).getStringCellValue());
            assertEquals("SPORT", dataRow.getCell(4).getStringCellValue());
        }
    }
}
