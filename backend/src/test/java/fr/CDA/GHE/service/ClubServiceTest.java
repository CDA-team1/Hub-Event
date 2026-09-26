package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Tests de {@link ClubService} avec une vraie base H2 (profil "test") et de vrais repositories.
 * <p>
 * Se concentre surtout sur {@link ClubService#delete(Long)} (CU24, SFG §2.27, règles n°2 à 6) :
 * logique métier centrale (cascade sur affiliations, rôles, événements), donc testée en détail
 * plutôt que dans les grandes lignes.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ClubServiceTest {

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    // --- create ---

    @Test
    void create_shouldPersistClub_whenDataValid() throws FunctionalException {
        ClubDto dto = new ClubDto(null, "Club Test", Category.SPORT, "1 rue de Test",
                "club@test.com", "0500000000", null, null);

        ClubDto created = clubService.create(dto);

        assertThat(created.id()).isNotNull();
        assertThat(clubRepository.findById(created.id())).isPresent();
    }

    @Test
    void create_shouldThrow_whenNameBlank() {
        ClubDto dto = new ClubDto(null, " ", Category.SPORT, "1 rue de Test",
                "club@test.com", "0500000000", null, null);

        assertThatThrownBy(() -> clubService.create(dto))
                .isInstanceOf(FunctionalException.class);
    }

    // --- update ---

    @Test
    void update_shouldModifyClub_whenDataValid() throws FunctionalException {
        Club club = persistClub("Ancien nom", Category.SPORT);

        ClubDto dto = new ClubDto(null, "Nouveau nom", Category.CULTURE, "2 rue de Test",
                "nouveau@test.com", "0500000001", null, null);

        ClubDto updated = clubService.update(club.getId(), dto);

        assertThat(updated.name()).isEqualTo("Nouveau nom");
        assertThat(updated.category()).isEqualTo(Category.CULTURE);
    }

    @Test
    void update_shouldThrow_whenClubNotFound() {
        ClubDto dto = new ClubDto(null, "Nom", Category.SPORT, "1 rue de Test",
                "club@test.com", "0500000000", null, null);

        assertThatThrownBy(() -> clubService.update(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    // --- extractAll / extractById ---

    @Test
    void extractAll_shouldReturnPageOfClubs() {
        persistClub("Club A", Category.SPORT);
        persistClub("Club B", Category.CULTURE);

        PageDto<ClubDto> page = clubService.extractAll(PageRequest.of(0, 10));

        assertThat(page.content()).hasSize(2);
    }

    @Test
    void extractById_shouldThrow_whenClubNotFound() {
        assertThatThrownBy(() -> clubService.extractById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    // --- delete : cas simple ---

    @Test
    void delete_shouldSetValidityEndDate_whenNoMembersOrEvents() throws FunctionalException {
        Club club = persistClub("Club vide", Category.SPORT);

        clubService.delete(club.getId());

        Club reloaded = clubRepository.findById(club.getId()).orElseThrow();
        assertThat(reloaded.getValidityEndDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void delete_shouldThrow_whenClubNotFound() {
        assertThatThrownBy(() -> clubService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldThrow_whenClubAlreadyDeleted() throws FunctionalException {
        Club club = persistClub("Club à supprimer", Category.SPORT);
        clubService.delete(club.getId());

        assertThatThrownBy(() -> clubService.delete(club.getId()))
                .isInstanceOf(FunctionalException.class);
    }

    // --- delete : règle n°2/3/4 (affiliations) ---

    @Test
    void delete_shouldRemoveAffiliation_butKeepMemberAffiliatedToOtherClub() throws FunctionalException {
        Club clubToDelete = persistClub("Club à supprimer", Category.SPORT);
        Club otherClub = persistClub("Autre club", Category.CULTURE);

        User member = persistUser("member@test.com", Role.MEMBER);
        clubToDelete.addMember(member);
        otherClub.addMember(member);

        clubService.delete(clubToDelete.getId());

        User reloaded = userRepository.findById(member.getId()).orElseThrow();
        assertThat(reloaded.getClubs()).containsExactly(otherClub);
        assertThat(reloaded.getRole()).isEqualTo(Role.MEMBER);
    }

    @Test
    void delete_shouldLeaveMemberNonAffiliated_whenItWasTheirOnlyClub() throws FunctionalException {
        Club club = persistClub("Club à supprimer", Category.SPORT);
        User member = persistUser("member@test.com", Role.MEMBER);
        club.addMember(member);

        clubService.delete(club.getId());

        User reloaded = userRepository.findById(member.getId()).orElseThrow();
        assertThat(reloaded.getClubs()).isEmpty();
        assertThat(reloaded.getRole()).isEqualTo(Role.MEMBER);
    }

    // --- delete : règle n°5 (organisateur) ---

    @Test
    void delete_shouldDowngradeOrganizerToMember_whenItWasTheirOnlyClub() throws FunctionalException {
        Club club = persistClub("Club à supprimer", Category.SPORT);
        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        club.addMember(organizer);

        clubService.delete(club.getId());

        User reloaded = userRepository.findById(organizer.getId()).orElseThrow();
        assertThat(reloaded.getClubs()).isEmpty();
        assertThat(reloaded.getRole()).isEqualTo(Role.MEMBER);
    }

    @Test
    void delete_shouldKeepOrganizerRole_whenAffiliatedToAnotherClub() throws FunctionalException {
        Club clubToDelete = persistClub("Club à supprimer", Category.SPORT);
        Club otherClub = persistClub("Autre club", Category.CULTURE);

        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        clubToDelete.addMember(organizer);
        otherClub.addMember(organizer);

        clubService.delete(clubToDelete.getId());

        User reloaded = userRepository.findById(organizer.getId()).orElseThrow();
        assertThat(reloaded.getRole()).isEqualTo(Role.ORGANIZER);
        assertThat(reloaded.getClubs()).containsExactly(otherClub);
    }

    // --- delete : règle n°6 (événements futurs) ---

    @Test
    void delete_shouldCancelUpcomingPublishedEvent_andSucceed() throws FunctionalException {
        Club club = persistClub("Club à supprimer", Category.SPORT);
        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        club.addMember(organizer);

        Event event = persistEvent(club, organizer, LocalDateTime.now().plusDays(5));
        event.publish();
        eventRepository.save(event);

        clubService.delete(club.getId());

        Event reloadedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloadedEvent.getStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(clubRepository.findById(club.getId()).orElseThrow().getValidityEndDate())
                .isEqualTo(LocalDate.now());
    }

    // --- delete : événements que le club garde en référence (historique préservé) ---

    @Test
    void delete_shouldNotAffectFinishedEvent_andSucceed() throws FunctionalException {
        Club club = persistClub("Club à supprimer", Category.SPORT);
        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        club.addMember(organizer);

        Event event = persistEvent(club, organizer, LocalDateTime.now().minusDays(5));
        event.publish();
        event.finish();
        eventRepository.save(event);

        clubService.delete(club.getId());

        Event reloadedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloadedEvent.getStatus()).isEqualTo(EventStatus.FINISHED);
        assertThat(reloadedEvent.getClub().getId()).isEqualTo(club.getId());
        assertThat(clubRepository.findById(club.getId())).isPresent();
    }

    @Test
    void delete_shouldNotAffectCancelledEvent_andSucceed() throws FunctionalException {
        Club club = persistClub("Club à supprimer", Category.SPORT);
        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        club.addMember(organizer);

        Event event = persistEvent(club, organizer, LocalDateTime.now().plusDays(5));
        event.publish();
        event.cancel();
        eventRepository.save(event);

        clubService.delete(club.getId());

        Event reloadedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloadedEvent.getStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(clubRepository.findById(club.getId())).isPresent();
    }

    @Test
    void delete_shouldNotAffectDraftEvent_andSucceed() throws FunctionalException {
        Club club = persistClub("Club à supprimer", Category.SPORT);
        User organizer = persistUser("organizer@test.com", Role.ORGANIZER);
        club.addMember(organizer);

        Event event = persistEvent(club, organizer, LocalDateTime.now().plusDays(5));
        // Reste au statut DRAFT (jamais publié).

        clubService.delete(club.getId());

        Event reloadedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloadedEvent.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(clubRepository.findById(club.getId())).isPresent();
    }

    private Club persistClub(String name, Category category) {
        return clubRepository.save(new Club(name, category, "1 rue de Test", name.toLowerCase().replace(" ", "-") + "@test.com", "0500000000"));
    }

    private User persistUser(String email, Role role) {
        User user = new User();
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPostalAddress("1 rue de Test");
        user.setEmail(email);
        user.setPassword("hashed-password");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Event persistEvent(Club club, User organizer, LocalDateTime startDateTime) {
        Event event = new Event("Titre", "Description", "Lieu",
                startDateTime, null,
                BigDecimal.TEN, BigDecimal.TEN, 50, Category.SPORT, organizer, club);
        return eventRepository.save(event);
    }
}
