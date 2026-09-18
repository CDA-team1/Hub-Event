package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.service.CalendarExcelService;
import fr.CDA.GHE.service.CalendarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Expose le calendrier personnel des utilisateurs authentifiés (CAL-01).
 */
@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final CalendarExcelService calendarExcelService;

    public CalendarController(
            CalendarService calendarService,
            CalendarExcelService calendarExcelService
    ) {
        this.calendarService = calendarService;
        this.calendarExcelService = calendarExcelService;
    }

    /**
     * Retourne les événements auxquels l'utilisateur connecté est inscrit, sur la période donnée.
     *
     * @param from début de la période
     * @param to   fin de la période
     * @return les événements correspondants, sous forme de cartes
     */
    @GetMapping
    public List<EventCardDto> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate to
            ) {
        return calendarService.getMyCalendar(from, to);
    }

    /**
     * Exporte le calendrier de l'utilisateur connecté au format Excel, sur la période donnée.
     *
     * @param from début de la période
     * @param to   fin de la période
     * @return fichier Excel (.xlsx) du calendrier
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] excel = calendarExcelService.generateExcel(from, to);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"calendrier.xlsx\"")
                .body(excel);
    }
}
