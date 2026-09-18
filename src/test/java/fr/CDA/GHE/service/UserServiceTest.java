package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.AdminUserRequest;
import fr.CDA.GHE.dto.ClubAffiliationRequest;
import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.CreateUserRequest;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.SuspendUserRequest;
import fr.CDA.GHE.dto.UpdateUserRequest;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests de {@link UserService} avec une vraie base H2 (profil "test", voir
 * {@code GheApplicationTests}) : vérifie le comportement réel des requêtes dérivées
 * (unicité d'email, jetons, affiliations) plutôt qu'un repository mocké — comme
 * {@code JpaUserDetailsServiceTest} / {@code LegalDocumentServiceTest}.
 * <p>
 * {@link EmailService} est mocké : on ne veut pas qu'un test tente une vraie connexion SMTP
 * (l'hôte configuré en profil "test" n'écoute nulle part) — même approche que
 * {@code EventServiceTest} qui mocke certaines dépendances au sein d'un contexte complet.
 * {@code @Transactional} annule les écritures après chaque test.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    private static final String STRONG_PASSWORD = "StrOng!Passw0rd";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // --- createUser (CU5 signup) ---

    @Test
    void createUser_shouldCreateInactiveMember_whenRequestIsValid() throws FunctionalException {
        CreateUserRequest request = new CreateUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, STRONG_PASSWORD);

        UserDto dto = userService.createUser(request);

        assertThat(dto.status()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(dto.role()).isEqualTo(Role.MEMBER);
        assertThat(userRepository.findByEmail("john.doe@test.com")).isPresent();
    }

    @Test
    void createUser_shouldThrow_whenRequiredFieldIsBlank() {
        CreateUserRequest request = new CreateUserRequest("", "John", "1 rue de Test",
                "john.doe@test.com", null, STRONG_PASSWORD);

        assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(FunctionalException.class);
    }

    @Test
    void createUser_shouldThrow_whenEmailAlreadyUsed() throws FunctionalException {
        userService.createUser(new CreateUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, STRONG_PASSWORD));

        CreateUserRequest duplicate = new CreateUserRequest("Autre", "Personne", "2 rue de Test",
                "john.doe@test.com", null, STRONG_PASSWORD);

        assertThatThrownBy(() -> userService.createUser(duplicate)).isInstanceOf(FunctionalException.class);
    }

    @Test
    void createUser_shouldThrow_whenPasswordIsWeak() {
        CreateUserRequest request = new CreateUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, "faible");

        assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(FunctionalException.class);
    }

    // --- activateAccount (CU6) ---

    @Test
    void activateAccount_shouldActivate_whenTokenIsValid() throws FunctionalException {
        User user = persistInactiveMemberWithToken("john.doe@test.com", "valid-token");

        UserDto dto = userService.activateAccount("valid-token");

        assertThat(dto.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(userRepository.findById(user.getId()).orElseThrow().getActivationToken()).isNull();
    }

    @Test
    void activateAccount_shouldThrow_whenTokenUnknown() {
        assertThatThrownBy(() -> userService.activateAccount("unknown-token"))
                .isInstanceOf(FunctionalException.class);
    }

    @Test
    void activateAccount_shouldThrow_whenAlreadyActive() throws FunctionalException {
        User user = persistInactiveMemberWithToken("john.doe@test.com", "valid-token");
        userService.activateAccount("valid-token");
        user.setActivationToken("valid-token"); // ré-injecté pour re-tester le contrôle de statut
        userRepository.save(user);

        assertThatThrownBy(() -> userService.activateAccount("valid-token"))
                .isInstanceOf(FunctionalException.class);
    }

    // --- updateOwnAccount / confirmPasswordChange (CU13) ---

    @Test
    void updateOwnAccount_shouldUpdateFields_whenEmailUnchanged() throws FunctionalException {
        User user = persistActiveMember("john.doe@test.com");
        authenticateAs(user);

        UpdateUserRequest request = new UpdateUserRequest("Doe", "Jonathan", "Nouvelle adresse",
                "john.doe@test.com", "0600000000", null);

        UserDto dto = userService.updateOwnAccount(request);

        assertThat(dto.firstName()).isEqualTo("Jonathan");
        assertThat(dto.postalAddress()).isEqualTo("Nouvelle adresse");
    }

    @Test
    void updateOwnAccount_shouldThrow_whenNewEmailAlreadyUsedByAnotherAccount() throws FunctionalException {
        persistActiveMember("taken@test.com");
        User user = persistActiveMember("john.doe@test.com");
        authenticateAs(user);

        UpdateUserRequest request = new UpdateUserRequest("Doe", "John", "1 rue de Test",
                "taken@test.com", null, null);

        assertThatThrownBy(() -> userService.updateOwnAccount(request)).isInstanceOf(FunctionalException.class);
    }

    @Test
    void updateOwnAccount_shouldThrow_whenAccountNotActive() {
        User user = persistInactiveMemberWithToken("john.doe@test.com", "some-token");
        authenticateAs(user);

        UpdateUserRequest request = new UpdateUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, null);

        assertThatThrownBy(() -> userService.updateOwnAccount(request)).isInstanceOf(FunctionalException.class);
    }

    @Test
    void updateOwnAccount_shouldThrow_whenPasswordProvidedIsWeak() {
        User user = persistActiveMember("john.doe@test.com");
        authenticateAs(user);

        UpdateUserRequest request = new UpdateUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, "faible");

        assertThatThrownBy(() -> userService.updateOwnAccount(request)).isInstanceOf(FunctionalException.class);
    }

    @Test
    void updateOwnAccount_shouldStagePassword_withoutApplyingItImmediately_whenPasswordProvided() throws FunctionalException {
        User user = persistActiveMember("john.doe@test.com");
        String originalPasswordHash = user.getPassword();
        authenticateAs(user);

        UpdateUserRequest request = new UpdateUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, STRONG_PASSWORD);

        userService.updateOwnAccount(request);

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getPassword()).isEqualTo(originalPasswordHash);
        assertThat(reloaded.getPendingPassword()).isNotNull();
        assertThat(reloaded.getPasswordChangeToken()).isNotNull();
    }

    @Test
    void confirmPasswordChange_shouldApplyPendingPassword_whenTokenIsValid() throws FunctionalException {
        User user = persistActiveMember("john.doe@test.com");
        authenticateAs(user);
        userService.updateOwnAccount(new UpdateUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, STRONG_PASSWORD));
        String token = userRepository.findById(user.getId()).orElseThrow().getPasswordChangeToken();

        userService.confirmPasswordChange(token);

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(STRONG_PASSWORD, reloaded.getPassword())).isTrue();
        assertThat(reloaded.getPendingPassword()).isNull();
        assertThat(reloaded.getPasswordChangeToken()).isNull();
    }

    @Test
    void confirmPasswordChange_shouldThrow_whenTokenInvalid() {
        assertThatThrownBy(() -> userService.confirmPasswordChange("unknown-token"))
                .isInstanceOf(FunctionalException.class);
    }

    // --- extractAll / extractById ---

    @Test
    void extractAll_shouldReturnPagedUsers() {
        persistActiveMember("a@test.com");
        persistActiveMember("b@test.com");

        PageDto<UserDto> page = userService.extractAll(PageRequest.of(0, 10));

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).hasSize(2);
    }

    @Test
    void extractById_shouldReturnUser_whenExists() {
        User user = persistActiveMember("john.doe@test.com");

        UserDto dto = userService.extractById(user.getId());

        assertThat(dto.email()).isEqualTo("john.doe@test.com");
    }

    @Test
    void extractById_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> userService.extractById(999L)).isInstanceOf(NotFoundException.class);
    }

    // --- createUserByAdmin / updateUserByAdmin (CU25) ---

    @Test
    void createUserByAdmin_shouldCreateInactiveAccount_withTemporaryPassword() throws FunctionalException {
        AdminUserRequest request = new AdminUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, Role.MEMBER, null);

        UserDto dto = userService.createUserByAdmin(request);

        assertThat(dto.status()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(dto.role()).isEqualTo(Role.MEMBER);
    }

    @Test
    void createUserByAdmin_shouldThrow_whenEmailAlreadyUsed() throws FunctionalException {
        userService.createUserByAdmin(new AdminUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, Role.MEMBER, null));

        AdminUserRequest duplicate = new AdminUserRequest("Autre", "Personne", "2 rue de Test",
                "john.doe@test.com", null, Role.MEMBER, null);

        assertThatThrownBy(() -> userService.createUserByAdmin(duplicate)).isInstanceOf(FunctionalException.class);
    }

    @Test
    void createUserByAdmin_shouldThrow_whenOrganizerHasNoClub() {
        AdminUserRequest request = new AdminUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, Role.ORGANIZER, null);

        assertThatThrownBy(() -> userService.createUserByAdmin(request)).isInstanceOf(FunctionalException.class);
    }

    @Test
    void createUserByAdmin_shouldAssignClubs_whenProvided() throws FunctionalException {
        Club club = persistClub("Club de Test");

        AdminUserRequest request = new AdminUserRequest("Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, Role.ORGANIZER, List.of(club.getId()));

        UserDto dto = userService.createUserByAdmin(request);

        assertThat(clubRepository.findByMembers_Id(dto.id())).extracting(Club::getId).containsExactly(club.getId());
    }

    @Test
    void updateUserByAdmin_shouldUpdateFields() throws FunctionalException {
        User user = persistActiveMember("john.doe@test.com");

        AdminUserRequest request = new AdminUserRequest("Doe", "Jonathan", "Nouvelle adresse",
                "john.doe@test.com", null, Role.MEMBER, null);

        UserDto dto = userService.updateUserByAdmin(user.getId(), request);

        assertThat(dto.firstName()).isEqualTo("Jonathan");
    }

    @Test
    void updateUserByAdmin_shouldThrow_whenEmailUsedByAnotherAccount() {
        persistActiveMember("taken@test.com");
        User user = persistActiveMember("john.doe@test.com");

        AdminUserRequest request = new AdminUserRequest("Doe", "John", "1 rue de Test",
                "taken@test.com", null, Role.MEMBER, null);

        assertThatThrownBy(() -> userService.updateUserByAdmin(user.getId(), request))
                .isInstanceOf(FunctionalException.class);
    }

    // --- updateMemberAffiliations (CU26/CU27) ---

    @Test
    void updateMemberAffiliations_shouldSyncClubs_forMember() throws FunctionalException {
        User member = persistActiveMember("john.doe@test.com");
        Club club = persistClub("Club de Test");

        List<ClubDto> result = userService.updateMemberAffiliations(member.getId(),
                new ClubAffiliationRequest(List.of(club.getId())));

        assertThat(result).extracting(ClubDto::id).containsExactly(club.getId());
    }

    @Test
    void updateMemberAffiliations_shouldKeepOrganizerRole_whenAtLeastOneClubRemains() throws FunctionalException {
        Club clubA = persistClub("Club A");
        Club clubB = persistClub("Club B");
        User organizer = persistActiveOrganizer("organizer@test.com", clubA);

        userService.updateMemberAffiliations(organizer.getId(),
                new ClubAffiliationRequest(List.of(clubB.getId())));

        assertThat(userRepository.findById(organizer.getId()).orElseThrow().getRole()).isEqualTo(Role.ORGANIZER);
    }

    @Test
    void updateMemberAffiliations_shouldDowngradeOrganizerToMember_whenNoClubsRemain() throws FunctionalException {
        Club club = persistClub("Club de Test");
        User organizer = persistActiveOrganizer("organizer@test.com", club);

        userService.updateMemberAffiliations(organizer.getId(), new ClubAffiliationRequest(List.of()));

        assertThat(userRepository.findById(organizer.getId()).orElseThrow().getRole()).isEqualTo(Role.MEMBER);
    }

    @Test
    void updateMemberAffiliations_shouldThrow_forAdmin() {
        User admin = persistActiveMember("admin@test.com");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        assertThatThrownBy(() -> userService.updateMemberAffiliations(admin.getId(),
                new ClubAffiliationRequest(List.of())))
                .isInstanceOf(FunctionalException.class);
    }

    // --- deleteUserByAdmin (CU25) ---

    @Test
    void deleteUserByAdmin_shouldRemoveAffiliationsThenDeleteAccount() {
        Club club = persistClub("Club de Test");
        User member = persistActiveMember("john.doe@test.com");
        club.getMembers().add(member);
        clubRepository.save(club);

        userService.deleteUserByAdmin(member.getId());

        assertThat(userRepository.findById(member.getId())).isEmpty();
        assertThat(clubRepository.findById(club.getId()).orElseThrow().getMembers()).isEmpty();
    }

    @Test
    void deleteUserByAdmin_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> userService.deleteUserByAdmin(999L)).isInstanceOf(NotFoundException.class);
    }

    // --- suspendUser (SUSP-01) ---

    @Test
    void suspendUser_shouldSuspendIndefinitely_whenNoEndDate() throws FunctionalException {
        User member = persistActiveMember("suspend1@test.com");

        userService.suspendUser(member.getId(), new SuspendUserRequest("Fraude avérée", null));

        User updated = userRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.isSuspended()).isTrue();
        assertThat(updated.getSuspensionEndDate()).isNull();
        assertThat(updated.getSuspensionReason()).isEqualTo("Fraude avérée");
        verify(emailService).sendAccountSuspendedEmail(member.getEmail(), "Fraude avérée", null);
    }

    @Test
    void suspendUser_shouldSuspendTemporarily_whenEndDateProvided() throws FunctionalException {
        User member = persistActiveMember("suspend2@test.com");
        LocalDate endDate = LocalDate.now().plusDays(7);

        userService.suspendUser(member.getId(), new SuspendUserRequest("Comportement inapproprié", endDate));

        User updated = userRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.isSuspended()).isTrue();
        assertThat(updated.getSuspensionEndDate()).isEqualTo(endDate);
        verify(emailService).sendAccountSuspendedEmail(member.getEmail(), "Comportement inapproprié", endDate);
    }

    @Test
    void suspendUser_shouldThrow_whenReasonMissing() {
        User member = persistActiveMember("suspend3@test.com");

        assertThatThrownBy(() -> userService.suspendUser(member.getId(), new SuspendUserRequest("  ", null)))
                .isInstanceOf(FunctionalException.class);

        assertThat(userRepository.findById(member.getId()).orElseThrow().isSuspended()).isFalse();
        verifyNoInteractions(emailService);
    }

    @Test
    void suspendUser_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> userService.suspendUser(999L, new SuspendUserRequest("Motif", null)))
                .isInstanceOf(NotFoundException.class);
    }

    // --- Fixtures ---

    private User persistActiveMember(String email) {
        User user = new User();
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPostalAddress("1 rue de Test");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(STRONG_PASSWORD));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);
        return userRepository.save(user);
    }

    private User persistActiveOrganizer(String email, Club club) {
        User user = persistActiveMember(email);
        user.setRole(Role.ORGANIZER);
        userRepository.save(user);
        club.getMembers().add(user);
        clubRepository.save(club);
        return user;
    }

    private User persistInactiveMemberWithToken(String email, String token) {
        User user = new User();
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPostalAddress("1 rue de Test");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(STRONG_PASSWORD));
        user.setStatus(AccountStatus.INACTIVE);
        user.setRole(Role.MEMBER);
        user.setActivationToken(token);
        return userRepository.save(user);
    }

    private Club persistClub(String name) {
        Club club = new Club(name, Category.SPORT, "1 rue du Club", "club@test.com", "0500000000");
        return clubRepository.save(club);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }
}
