package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.RegistrationDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.ForbiddenException;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.RegistrationRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Tests de {@link RegistrationService#register} avec une vraie base H2 (profil "test", voir
 * {@code GheApplicationTests}) : couvre notamment le contrôle du statut de l'événement
 * (REG-09) — {@code register} ne doit accepter une nouvelle inscription que pour un
 * événement {@code PUBLISHED} (trou de couverture précédemment signalé dans
 * {@code RegistrationControllerTest}).
 * <p>
 * {@code @Transactional} annule les écritures après chaque test.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldCreateRegistration_whenEventIsPublished() throws FunctionalException {
        User member = persistActiveUser("member@test.com", "Doe", "John");
        Event event = persistPublishedEvent(persistActiveUser("organizer@test.com", "Smith", "Ana"));
        authenticateAs(member);

        RegistrationDto dto = registrationService.register(event.getId());

        assertThat(dto.status()).isEqualTo(RegistrationStatus.REGISTERED);
        assertThat(registrationRepository.findByUser(member)).hasSize(1);
    }

    @Test
    void register_shouldThrow_whenEventIsDraft() {
        User member = persistActiveUser("member@test.com", "Doe", "John");
        Event event = persistEvent(persistActiveUser("organizer@test.com", "Smith", "Ana"));
        authenticateAs(member);

        assertThatThrownBy(() -> registrationService.register(event.getId()))
                .isInstanceOf(FunctionalException.class);
        assertThat(registrationRepository.findByUser(member)).isEmpty();
    }

    @Test
    void register_shouldThrow_whenEventIsCancelled() {
        User member = persistActiveUser("member@test.com", "Doe", "John");
        Event event = persistEvent(persistActiveUser("organizer@test.com", "Smith", "Ana"));
        event.publish();
        event.cancel();
        eventRepository.save(event);
        authenticateAs(member);

        assertThatThrownBy(() -> registrationService.register(event.getId()))
                .isInstanceOf(FunctionalException.class);
        assertThat(registrationRepository.findByUser(member)).isEmpty();
    }

    @Test
    void register_shouldThrow_whenEventIsFinished() {
        User member = persistActiveUser("member@test.com", "Doe", "John");
        Event event = persistEvent(persistActiveUser("organizer@test.com", "Smith", "Ana"));
        event.publish();
        event.finish();
        eventRepository.save(event);
        authenticateAs(member);

        assertThatThrownBy(() -> registrationService.register(event.getId()))
                .isInstanceOf(FunctionalException.class);
        assertThat(registrationRepository.findByUser(member)).isEmpty();
    }

    @Test
    void register_shouldThrow_whenEventHasAlreadyStarted() {
        User member = persistActiveUser("member@test.com", "Doe", "John");
        Event event = persistEvent(persistActiveUser("organizer@test.com", "Smith", "Ana"),
                LocalDateTime.now().minusHours(1), 50);
        event.publish();
        eventRepository.save(event);
        authenticateAs(member);

        assertThatThrownBy(() -> registrationService.register(event.getId()))
                .isInstanceOf(FunctionalException.class)
                .hasMessage("Les inscriptions sont closes : cet événement a déjà commencé.");
        assertThat(registrationRepository.findByUser(member)).isEmpty();
    }

    @Test
    void register_shouldPutOnWaitingList_whenEventIsFull() throws FunctionalException {
        User first = persistActiveUser("first@test.com", "Doe", "John");
        User second = persistActiveUser("second@test.com", "Roe", "Jane");
        Event event = persistEvent(persistActiveUser("organizer@test.com", "Smith", "Ana"),
                LocalDateTime.now().plusDays(1), 1);
        event.publish();
        eventRepository.save(event);

        authenticateAs(first);
        RegistrationDto firstRegistration = registrationService.register(event.getId());
        authenticateAs(second);
        RegistrationDto secondRegistration = registrationService.register(event.getId());

        assertThat(firstRegistration.status()).isEqualTo(RegistrationStatus.REGISTERED);
        assertThat(secondRegistration.status()).isEqualTo(RegistrationStatus.WAITING_LIST);
    }

    @Test
    void register_shouldThrow_whenOverlappingAnotherRegistration() throws FunctionalException {
        User member = persistActiveUser("member@test.com", "Doe", "John");
        User organizer = persistActiveUser("organizer@test.com", "Smith", "Ana");
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        Event first = persistPublishedEvent(organizer, start, start.plusHours(3));
        Event second = persistPublishedEvent(organizer, start.plusHours(1), start.plusHours(4));
        authenticateAs(member);
        registrationService.register(first.getId());

        assertThatThrownBy(() -> registrationService.register(second.getId()))
                .isInstanceOf(FunctionalException.class)
                .hasMessage("Vous êtes déjà inscrit à un autre événement sur ce créneau horaire.");
    }

    @Test
    void register_shouldAllow_whenOverlappingRegistrationIsOnACancelledEvent() throws FunctionalException {
        User member = persistActiveUser("member@test.com", "Doe", "John");
        User organizer = persistActiveUser("organizer@test.com", "Smith", "Ana");
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        Event cancelled = persistPublishedEvent(organizer, start, start.plusHours(3));
        Event other = persistPublishedEvent(organizer, start.plusHours(1), start.plusHours(4));
        authenticateAs(member);
        registrationService.register(cancelled.getId());
        cancelled.cancel();
        eventRepository.save(cancelled);

        RegistrationDto dto = registrationService.register(other.getId());

        assertThat(dto.status()).isEqualTo(RegistrationStatus.REGISTERED);
    }

    @Test
    void getRegistrationsForEvent_shouldReturnRegistrations_whenOrganizerIsOwner() throws FunctionalException {
        User organizer = persistActiveUser("organizer@test.com", "Smith", "Ana");
        User member = persistActiveUser("member@test.com", "Doe", "John");
        Event event = persistPublishedEvent(organizer);

        authenticateAs(member);
        registrationService.register(event.getId());

        authenticateAs(organizer);
        List<RegistrationDto> result = registrationService.getRegistrationsForEvent(event.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userEmail()).isEqualTo("member@test.com");
        assertThat(result.get(0).userId()).isEqualTo(member.getId());
    }

    @Test
    void getRegistrationsForEvent_shouldThrow_whenCurrentUserIsNotOwner() {
        User organizer = persistActiveUser("organizer@test.com", "Smith", "Ana");
        User otherOrganizer = persistActiveUser("other@test.com", "Martin", "Lea");
        Event event = persistPublishedEvent(organizer);

        authenticateAs(otherOrganizer);

        assertThatThrownBy(() -> registrationService.getRegistrationsForEvent(event.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    private User persistActiveUser(String email, String lastName, String firstName) {
        User user = new User();
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setPostalAddress("1 rue de Test");
        user.setEmail(email);
        user.setPassword("hashed-password");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);
        return userRepository.save(user);
    }

    private Event persistEvent(User organizer) {
        return persistEvent(organizer, LocalDateTime.now().plusDays(1), 50);
    }

    private Event persistEvent(User organizer, LocalDateTime start, int maxSeats) {
        return persistEvent(organizer, start, null, maxSeats);
    }

    private Event persistEvent(User organizer, LocalDateTime start, LocalDateTime end, int maxSeats) {
        Club club = clubRepository.save(new Club("Club de Test", Category.SPORT,
                "1 rue du Club", "club@test.com", "0500000000"));

        Event event = new Event("Titre", "Description", "Lieu",
                start, end,
                BigDecimal.TEN, BigDecimal.TEN, maxSeats, Category.SPORT, organizer, club);
        return eventRepository.save(event);
    }

    private Event persistPublishedEvent(User organizer) {
        Event event = persistEvent(organizer);
        event.publish();
        return eventRepository.save(event);
    }

    private Event persistPublishedEvent(User organizer, LocalDateTime start, LocalDateTime end) {
        Event event = persistEvent(organizer, start, end, 50);
        event.publish();
        return eventRepository.save(event);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }
}
