package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.service.CalendarService;
import org.springframework.format.annotation.DateTimeFormat;
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

    public CalendarController(CalendarService calendarService)
    {
        this.calendarService = calendarService;
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
}
