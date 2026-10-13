package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.AdminAnonymizationDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.entity.AnonymizationRequest;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.RegistrationRepository;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.RequestStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.AnonymizationRequestRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AnonymizationRequestServiceTest {

    @Autowired
    private AnonymizationRequestService anonymizationRequestService;

    @Autowired
    private AnonymizationRequestRepository anonymizationRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @MockitoBean
    private EmailService emailService;

    @Test
    void extractAll_shouldReturnOnlyPendingRequests() {
        User pendingUser = persistUser("pending@test.com");
        User validatedUser = persistUser("validated@test.com");

        anonymizationRequestRepository.save(
                new AnonymizationRequest(
                        pendingUser,
                        RequestStatus.PENDING,
                        LocalDateTime.now()
                )
        );

        anonymizationRequestRepository.save(
                new AnonymizationRequest(
                        validatedUser,
                        RequestStatus.VALIDATED,
                        LocalDateTime.now()
                )
        );

        PageDto<AdminAnonymizationDto> result =
                anonymizationRequestService.extractAll(PageRequest.of(0, 10));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().status()).isEqualTo(RequestStatus.PENDING);
        assertThat(result.content().getFirst().user().email()).isEqualTo("pending@test.com");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    private User persistUser(String email) {
        return persistUser(email, Role.MEMBER);
    }

    private User persistUser(String email, Role role) {
        User user = new User(
                "Doe",
                "Jane",
                "1 rue de Test",
                email,
                "0600000000",
                "hashed-password",
                role
        );
        user.setStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    private Event persistPublishedEvent(User organizer, LocalDateTime start, int maxSeats) {
        Club club = clubRepository.save(new Club("Club Test", Category.SPORT,
                "10 rue du Club", "club@test.com", "0611111111"));
        Event event = new Event("Titre", "Description", "Lieu", start, null,
                BigDecimal.TEN, BigDecimal.TEN, maxSeats, Category.SPORT, organizer, club);
        event.publish();
        return eventRepository.save(event);
    }

    private void register(User user, Event event, RegistrationStatus status, int minutesAfterNow) {
        registrationRepository.save(new Registration(user, event, status,
                LocalDateTime.now().plusMinutes(minutesAfterNow)));
    }

    private AnonymizationRequest persistPendingRequest(User user) {
        return anonymizationRequestRepository.save(
                new AnonymizationRequest(user, RequestStatus.PENDING, LocalDateTime.now()));
    }

    @Test
    void extractAll_shouldIncludeAffiliatedClubs() {
         User user = persistUser("affiliated@test.com");

        Club club = new Club(
                "Club Test",
                Category.SPORT,
                "10 rue du Club",
                "club@test.com",
                "0611111111"
        );

        club.getMembers().add(user);
        clubRepository.save(club);

        anonymizationRequestRepository.save(
                 new AnonymizationRequest(
                         user,
                         RequestStatus.PENDING,
                        LocalDateTime.now()
                )
        );

        PageDto<AdminAnonymizationDto> result =
                anonymizationRequestService.extractAll(PageRequest.of(0, 10));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().user().clubs()).hasSize(1);
        assertThat(result.content().getFirst().user().clubs().getFirst().name())
                .isEqualTo("Club Test");
        }

    @Test
    void validateRequest_shouldCancelOrganizerUpcomingEvents_andNotifyRegistrantsAndWaitingList()
            throws FunctionalException {
        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        User registered = persistUser("registered@test.com");
        User waiting = persistUser("waiting@test.com");
        Event event = persistPublishedEvent(organizer, LocalDateTime.now().plusDays(5), 1);
        register(registered, event, RegistrationStatus.REGISTERED, 0);
        register(waiting, event, RegistrationStatus.WAITING_LIST, 1);
        AnonymizationRequest request = persistPendingRequest(organizer);

        anonymizationRequestService.validateRequest(request.getId());

        assertThat(eventRepository.findById(event.getId()).orElseThrow().getStatus())
                .isEqualTo(EventStatus.CANCELLED);
        verify(emailService).sendEventCancelledEmail("registered@test.com", "Titre");
        verify(emailService).sendEventCancelledEmail("waiting@test.com", "Titre");
    }

    @Test
    void validateRequest_shouldRemoveUpcomingRegistrations_andPromoteFirstOnWaitingList()
            throws FunctionalException {
        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        User leaving = persistUser("leaving@test.com");
        User waiting = persistUser("waiting@test.com");
        Event event = persistPublishedEvent(organizer, LocalDateTime.now().plusDays(5), 1);
        register(leaving, event, RegistrationStatus.REGISTERED, 0);
        register(waiting, event, RegistrationStatus.WAITING_LIST, 1);
        AnonymizationRequest request = persistPendingRequest(leaving);

        anonymizationRequestService.validateRequest(request.getId());

        assertThat(registrationRepository.findByUserAndEvent(leaving, event)).isEmpty();
        assertThat(registrationRepository.findByUserAndEvent(waiting, event).orElseThrow().getStatus())
                .isEqualTo(RegistrationStatus.REGISTERED);
        verify(emailService).sendWaitingListPromotionEmail("waiting@test.com", "Titre");
    }

    @Test
    void validateRequest_shouldKeepRegistrationsOnFinishedEvents() throws FunctionalException {
        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        User member = persistUser("member@test.com");
        Event event = persistPublishedEvent(organizer, LocalDateTime.now().minusDays(3), 10);
        event.finish();
        eventRepository.save(event);
        register(member, event, RegistrationStatus.REGISTERED, -4000);
        AnonymizationRequest request = persistPendingRequest(member);

        anonymizationRequestService.validateRequest(request.getId());

        assertThat(registrationRepository.findByUserAndEvent(member, event)).isPresent();
    }
}