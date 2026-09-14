package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.LoginRequest;
import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link AuthService}, avec {@link UserRepository}, {@link AuthenticationManager}
 * et {@link JwtService} mockés (pas de contexte Spring chargé).
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    private static final String RAW_PASSWORD = "StrOng!PasswOrd";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, authenticationManager, jwtService);
    }

    /**
     * Vérifie qu'un utilisateur actif avec les bons identifiants peut se connecter,
     * et récupère bien le jeton généré par {@link JwtService}.
     */
    @Test
    void login_shouldSucceed_whenCredentialsAreValidAndAccountIsActive() throws FunctionalException {
        User user = new User();
        user.setEmail("secret.story@test.com");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);

        when(userRepository.findByEmail("secret.story@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("secret.story@test.com", Role.MEMBER)).thenReturn("fake-jwt-token");

        LoginRequest request = new LoginRequest("secret.story@test.com", RAW_PASSWORD);
        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.role()).isEqualTo(Role.MEMBER);
    }

    /**
     * Vérifie qu'un échec d'authentification (email inconnu ou mot de passe incorrect)
     * est rejeté. Les deux cas sont désormais indissociables du point de vue d'AuthService,
     * car c'est l'AuthenticationManager de Spring Security (via JpaUserDetailsService) qui
     * lève la même exception générique dans les deux cas.
     */
    @Test
    void login_shouldThrow_whenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

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
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);
        user.setSuspended(true);

        when(userRepository.findByEmail("secret.story@test.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("secret.story@test.com", RAW_PASSWORD);

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(FunctionalException.class);
    }
}
