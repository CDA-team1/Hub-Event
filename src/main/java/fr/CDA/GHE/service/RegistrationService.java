package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.RegistrationDto;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.RegistrationMapper;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.RegistrationRepository;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.util.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gère l'inscription des utilisateurs aux évènements.
 */
@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationMapper registrationMapper;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               UserRepository userRepository,
                               RegistrationMapper registrationMapper) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationMapper = registrationMapper;
    }

    /**
     * Inscrit l'utilisateur connecté à l'évènement donné.
     * <p>
     * Règles vérifiées, dans l'ordre : le compte doit être {@link AccountStatus#ACTIVE},
     * l'utilisateur ne doit pas déjà être inscrit à cet évènement, et il ne doit pas avoir
     * d'inscription sur un évènement se déroulant sur un créneau qui chevauche celui-ci.
     * Le statut de la nouvelle inscription dépend ensuite des places restantes :
     * {@link RegistrationStatus#REGISTERED} s'il reste de la place, sinon
     * {@link RegistrationStatus#WAITING_LIST}.
     * </p>
     *
     * @param eventId identifiant de l'évènement auquel s'inscrire
     * @return l'inscription créée
     * @throws FunctionalException si le compte n'est pas actif, si l'utilisateur est déjà
     *                             inscrit, ou en cas de chevauchement avec un autre évènement
     */
    @Transactional
    public RegistrationDto register(Long eventId) throws FunctionalException {

        User user = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new FunctionalException("Le compte doit être actif pour s'inscrire à un évènement");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Évènement introuvable"));

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new FunctionalException("Vous êtes déjà inscrit à cet évènement");
        }

        if (hasOverlappingRegistration(user, event)) {
            throw new FunctionalException("Vous êtes déjà inscrit à un évènement se déroulant sur le même créneau");
        }

        long registeredCount = registrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
        RegistrationStatus status = registeredCount < event.getMaxSeats()
                ? RegistrationStatus.REGISTERED
                : RegistrationStatus.WAITING_LIST;

        Registration registration = new Registration(user, event, status, LocalDateTime.now());
        Registration saved = registrationRepository.save(registration);

        return registrationMapper.toDto(saved);
    }

    private boolean hasOverlappingRegistration(User user, Event newEvent) {
        LocalDateTime newStart = newEvent.getStartDateTime();
        LocalDateTime newEnd = newEvent.getEndDateTime() != null ? newEvent.getEndDateTime() : newStart;

        List<Registration> existingRegistrations = registrationRepository.findByUser(user);

        for (Registration existing : existingRegistrations) {
            Event existingEvent = existing.getEvent();
            LocalDateTime existingStart = existingEvent.getStartDateTime();
            LocalDateTime existingEnd = existingEvent.getEndDateTime() != null
                    ? existingEvent.getEndDateTime()
                    : existingStart;

            boolean overlaps = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
            if (overlaps) {
                return true;
            }
        }
        return false;
    }
}