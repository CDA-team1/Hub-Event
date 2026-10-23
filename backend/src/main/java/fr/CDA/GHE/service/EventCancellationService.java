package fr.CDA.GHE.service;

import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Annulation d'un événement publié avec notification de ses inscrits et de sa liste d'attente.
 * <p>
 * Partagée par tous les cas où un événement passe à {@code CANCELLED} : annulation par son
 * organisateur (CU20), suppression de son club (CU24) et anonymisation de son organisateur
 * (CU29). Sans ce point commun, seuls les inscrits du premier cas étaient prévenus.
 * </p>
 * <p>
 * Pas de {@code @Transactional} ici : la méthode s'exécute dans la transaction de l'appelant.
 * </p>
 */
@Service
public class EventCancellationService {

    private final RegistrationRepository registrationRepository;
    private final EmailService emailService;

    public EventCancellationService(RegistrationRepository registrationRepository,
                                    EmailService emailService) {
        this.registrationRepository = registrationRepository;
        this.emailService = emailService;
    }

    /**
     * Passe l'événement au statut {@code CANCELLED} puis prévient par email toutes les personnes
     * inscrites, y compris celles en liste d'attente. Les inscriptions sont conservées.
     *
     * @param event événement à annuler (entité gérée par la transaction de l'appelant)
     */
    public void cancelAndNotify(Event event) {
        List<Registration> registrations = registrationRepository.findByEvent(event);

        event.cancel();

        for (Registration registration : registrations) {
            emailService.sendEventCancelledEmail(registration.getUser().getEmail(), event.getTitle());
        }
    }
}
