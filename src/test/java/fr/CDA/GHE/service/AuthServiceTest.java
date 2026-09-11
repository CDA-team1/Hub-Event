package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.LoginRequest;
import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link AuthService}, avec un {@link UserRepository} mocké
 * et un vrai {@link BCryptPasswordEncoder} (pas de contexte Spring chargé).
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    private static final String RAW_PASSWORD = "StrOng!PasswOrd";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder);
    }

    /**
     * Vérifie qu'un utilisateur actif avec les bons identifiants peut se connecter.
     */
    @Test
    void login_shouldSucceed_whenCredentialsAreValidAndAccountIsActive() throws FunctionalException {
        User user = new User();
        user.setEmail("secret.story@test.com");
        user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);

        when(userRepository.findByEmail("secret.story@test.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("secret.story@test.com", RAW_PASSWORD);
        LoginResponse response = authService.login(request);

        assertThat(response.role()).isEqualTo(Role.MEMBER);
    }

    /**
     * Vérifie qu'un email inconnu en base est rejeté.
     */
    @Test
    void login_shouldThrow_whenEmailNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown@test.com", RAW_PASSWORD);

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(FunctionalException.class);
    }

    /**
     * Vérifie qu'un mot de passe incorrect est rejeté.
     */
    @Test
    void login_shouldThrow_whenPasswordIncorrect() {
        User user = new User();
        user.setEmail("secret.story@test.com");
        user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);

        when(userRepository.findByEmail("secret.story@test.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("secret.story@test.com", "MauvaisMotDePasse123!");

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(FunctionalException.class);
    }

    /**
     * Vérifie qu'un compte qui n'est pas au statut ACTIVE (ex : INACTIVE) est rejeté.
     */
    @Test
    void login_shouldThrow_whenAccountNotActive() {
        User user = new User();
        user.setEmail("secret.story@test.com");
        user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        user.setStatus(AccountStatus.INACTIVE);
        user.setRole(Role.MEMBER);

        when(userRepository.findByEmail("secret.story@test.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("secret.story@test.com", RAW_PASSWORD);

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(FunctionalException.class);
    }

    /**
     * Vérifie qu'un compte suspendu de façon définitive (pas de date de fin) est rejeté.
     */
    @Test
    void login_shouldThrow_whenAccountSuspended() {
        User user = new User();
        user.setEmail("secret.story@test.com");
        user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);
        user.setSuspended(true);

        when(userRepository.findByEmail("secret.story@test.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("secret.story@test.com", RAW_PASSWORD);

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(FunctionalException.class);
    }
}
