package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.EventMapper;
import fr.CDA.GHE.repository.RegistrationRepository;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.util.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * Service gérant le calendrier personnel des utilisateurs (CAL-01).
 */
@Service
public class CalendarService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    public CalendarService(
            RegistrationRepository registrationRepository,
            UserRepository userRepository,
            EventMapper eventMapper
    ){
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
    }

    /**
     * Retourne les événements auxquels l'utilisateur courant est inscrit (statut REGISTERED),
     * dont la période chevauche celle demandée.
     *
     * @param from début de la période recherchée
     * @param to   fin de la période recherchée
     * @return les événements correspondants, triés chronologiquement, sous forme de cartes
     */
    @Transactional(readOnly = true)
    public List<EventCardDto> getMyCalendar(LocalDate from, LocalDate to){

        User user = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        return registrationRepository.findRegisteredByUserAndPeriod(user, fromDateTime,
                toDateTime).stream()
                .map(Registration::getEvent)
                .sorted(Comparator.comparing(Event::getStartDateTime))
                .map(eventMapper::toCardDto)
                .toList();
    }
}
