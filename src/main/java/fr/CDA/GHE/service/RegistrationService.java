package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.RegistrationDto;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import fr.CDA.GHE.exception.ForbiddenException;
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
 * Gère l'inscription et la désinscription des utilisateurs aux évènements.
 */
@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationMapper registrationMapper;
    private final EmailService emailService;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               UserRepository userRepository,
                               RegistrationMapper registrationMapper,
                               EmailService emailService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationMapper = registrationMapper;
        this.emailService = emailService;
    }

    /**
     * Inscrit l'utilisateur connecté à l'événement donné.
     * <p>
     * Règles vérifiées, dans l'ordre : le compte doit être {@link AccountStatus#ACTIVE},
     * l'utilisateur ne doit pas déjà être inscrit à cet événement, et il ne doit pas avoir
     * d'inscription sur un événement se déroulant sur un créneau qui chevauche celui-ci.
     * Le statut de la nouvelle inscription dépend ensuite des places restantes :
     * {@link RegistrationStatus#REGISTERED} s'il reste de la place, sinon
     * {@link RegistrationStatus#WAITING_LIST}.
     * </p>
     * <p>
     * Voir Dossier des spécifications générales, cas d'utilisation n°9 « S'inscrire à un
     * événement », pour le texte officiel des messages d'erreur.
     * </p>
     *
     * @param eventId identifiant de l'événement auquel s'inscrire
     * @return l'inscription créée
     * @throws FunctionalException si le compte n'est pas actif, si l'utilisateur est déjà
     *                             inscrit, ou en cas de chevauchement avec un autre événement
     */
    @Transactional
    public RegistrationDto register(Long eventId) throws FunctionalException {

        User user = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new FunctionalException("Votre compte doit être actif pour vous inscrire à un événement.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new FunctionalException("Vous êtes déjà inscrit à cet événement.");
        }

        if (hasOverlappingRegistration(user, event)) {
            throw new FunctionalException("Vous êtes déjà inscrit à un autre événement sur ce créneau horaire.");
        }

        long registeredCount = registrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
        RegistrationStatus status = registeredCount < event.getMaxSeats()
                ? RegistrationStatus.REGISTERED
                : RegistrationStatus.WAITING_LIST;

        Registration registration = new Registration(user, event, status, LocalDateTime.now());
        Registration saved = registrationRepository.save(registration);

        return registrationMapper.toDto(saved);
    }

    /**
     * Désinscrit l'utilisateur connecté de l'événement donné.
     * <p>
     * Si l'inscription supprimée était {@link RegistrationStatus#REGISTERED} (donc occupait
     * une place) et qu'il y a des personnes en liste d'attente, la première d'entre elles
     * (la plus ancienne inscrite) est automatiquement promue au statut
     * {@link RegistrationStatus#REGISTERED}, et un email de notification lui est envoyé.
     * </p>
     * <p>
     * Voir Dossier des spécifications générales, cas d'utilisation n°10 « Se désinscrire
     * d'un événement », pour le texte officiel des messages d'erreur.
     * </p>
     *
     * @param eventId identifiant de l'événement à quitter
     * @throws FunctionalException si l'utilisateur ou l'événement n'existe pas,
     *                             ou si l'utilisateur n'est pas inscrit à cet événement
     */
    @Transactional
    public void unregister(Long eventId) throws FunctionalException {

        User user = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        Registration registration = registrationRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new FunctionalException("Vous n'êtes pas inscrit à cet événement."));

        boolean freesASeat = registration.getStatus() == RegistrationStatus.REGISTERED;

        registrationRepository.delete(registration);

        if (freesASeat) {
            promoteFirstWaitingListRegistration(event);
        }
    }


    /**
     * Annule l'inscription d'un membre à la demande de l'organisateur de l'évènement (CU22).
     * <p>
     * Seul l'organisateur propriétaire de l'évènement peut effectuer cette action. Le membre
     * désinscrit reçoit un email contenant le motif saisi. Si l'inscription supprimée occupait
     * une place, la première personne en liste d'attente est automatiquement promue et notifiée.
     * </p>
     *
     * @param eventId identifiant de l'évènement concerné
     * @param userId  identifiant du membre dont l'inscription doit être annulée
     * @param reason  motif de l'annulation, obligatoire
     * @throws ForbiddenException  si l'utilisateur connecté n'est pas l'organisateur de l'évènement
     * @throws FunctionalException si le motif est manquant ou si le membre n'est pas inscrit
     */
    @Transactional
    public void cancelRegistrationByOrganizer(Long eventId, Long userId, String reason) throws FunctionalException{

        User currentUser = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        if (!event.getOrganizer().getId().equals(currentUser.getId())){
            throw new ForbiddenException("Vous n'êtes pas autorisé à annuler cette incription");
        }

        if (reason == null || reason.isBlank()){
            throw new FunctionalException("Le motif de l'annulation est obligatoire");
        }

        User member = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Registration registration = registrationRepository.findByUserAndEvent(member, event)
                .orElseThrow(() -> new FunctionalException("Ce membre n'est pas inscrit à cet événement"));

        boolean freesASeat = registration.getStatus() == RegistrationStatus.REGISTERED;

        registrationRepository.delete(registration);

        emailService.sendRegistrationCancelledEmail(member.getEmail(), event.getTitle(), reason);

        if (freesASeat){
            promoteFirstWaitingListRegistration(event);
        }
    }

    private void promoteFirstWaitingListRegistration(Event event) {
        registrationRepository.findFirstByEventAndStatusOrderByRegistrationDateAsc(event, RegistrationStatus.WAITING_LIST)
                .ifPresent(waiting -> {
                    waiting.setStatus(RegistrationStatus.REGISTERED);
                    registrationRepository.save(waiting);
                    emailService.sendWaitingListPromotionEmail(waiting.getUser().getEmail(), event.getTitle());
                });
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