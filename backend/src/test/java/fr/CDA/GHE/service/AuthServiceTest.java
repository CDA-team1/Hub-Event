package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.LoginRequest;
import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.UnauthorizedException;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Tests de {@link AuthService} avec une vraie base H2 et le vrai {@code AuthenticationManager}
 * (profil "test", voir {@code GheApplicationTests}) : donc le vrai {@link JpaUserDetailsService}
 * et le vrai {@link PasswordEncoder}. Contrairement à un {@code AuthenticationManager} mocké,
 * ça vérifie que la validation d'un mot de passe fonctionne réellement (BCrypt), pas seulement
 * que le service réagit comme prévu à ce qu'un mock lui dit de faire — même logique que
 * {@code JpaUserDetailsServiceTest} / {@code UserServiceTest}.
 * <p>
 * {@code @Transactional} annule les écritures après chaque test.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String RAW_PASSWORD = "StrOng!PasswOrd";

    /**
     * Vérifie qu'un utilisateur actif avec les bons identifiants peut se connecter, et que le
     * mot de passe fourni est réellement comparé au hash BCrypt stocké (pas un mock qui laisse
     * passer sans vérifier).
     */
    @Test
    void login_shouldSucceed_whenCredentialsAreValidAndAccountIsActive() {
        persistUser("secret.story@test.com", AccountStatus.ACTIVE, false, null);

        LoginResponse response = authService.login(new LoginRequest("secret.story@test.com", RAW_PASSWORD));

        assertThat(response.token()).isNotBlank();
        assertThat(response.role()).isEqualTo(Role.MEMBER);
    }

    /**
     * Vérifie qu'un mot de passe incorrect est rejeté par la vraie vérification BCrypt.
     */
    @Test
    void login_shouldThrow_whenPasswordIsIncorrect() {
        persistUser("secret.story@test.com", AccountStatus.ACTIVE, false, null);

        assertThatThrownBy(() -> authService.login(new LoginRequest("secret.story@test.com", "MauvaisMotDePasse123!")))
                .isInstanceOf(UnauthorizedException.class);
    }

    /**
     * Vérifie qu'un email inconnu est rejeté (même message générique que mot de passe
     * incorrect, pour éviter l'énumération de comptes).
     */
    @Test
    void login_shouldThrow_whenEmailIsUnknown() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@test.com", RAW_PASSWORD)))
                .isInstanceOf(UnauthorizedException.class);
    }

    /**
     * Vérifie qu'un compte qui n'est pas au statut ACTIVE (ex : INACTIVE) est rejeté.
     */
    @Test
    void login_shouldThrow_whenAccountNotActive() {
        persistUser("secret.story@test.com", AccountStatus.INACTIVE, false, null);

        assertThatThrownBy(() -> authService.login(new LoginRequest("secret.story@test.com", RAW_PASSWORD)))
                .isInstanceOf(UnauthorizedException.class);
    }

    /**
     * Vérifie qu'un compte suspendu de façon définitive (pas de date de fin) est rejeté.
     */
    @Test
    void login_shouldThrow_whenAccountSuspendedIndefinitely() {
        persistUser("secret.story@test.com", AccountStatus.ACTIVE, true, null);

        assertThatThrownBy(() -> authService.login(new LoginRequest("secret.story@test.com", RAW_PASSWORD)))
                .isInstanceOf(UnauthorizedException.class);
    }

    /**
     * Vérifie qu'une suspension temporaire déjà terminée ne bloque plus la connexion.
     */
    @Test
    void login_shouldSucceed_whenTemporarySuspensionHasEnded() {
        persistUser("secret.story@test.com", AccountStatus.ACTIVE, true, LocalDate.now().minusDays(1));

        LoginResponse response = authService.login(new LoginRequest("secret.story@test.com", RAW_PASSWORD));

        assertThat(response.token()).isNotBlank();
    }

    private User persistUser(String email, AccountStatus status, boolean suspended, LocalDate suspensionEndDate) {
        User user = new User();
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPostalAddress("1 rue de Test");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        user.setStatus(status);
        user.setRole(Role.MEMBER);
        user.setSuspended(suspended);
        user.setSuspensionEndDate(suspensionEndDate);
        return userRepository.save(user);
    }
}
