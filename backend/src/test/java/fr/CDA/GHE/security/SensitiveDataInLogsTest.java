package fr.CDA.GHE.security;

import fr.CDA.GHE.LogCapture;
import fr.CDA.GHE.dto.CreateUserRequest;
import fr.CDA.GHE.dto.LoginRequest;
import fr.CDA.GHE.dto.UpdateUserRequest;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.exception.UnauthorizedException;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.service.AuthService;
import fr.CDA.GHE.service.EmailService;
import fr.CDA.GHE.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Parcours complet inscription, activation, connexion, changement de mot de passe, avec tous les
 * journaux de l'application écoutés (SEC-14) : aucun mot de passe, JWT, jeton de lien ni adresse
 * email complète ne doit y apparaître, alors que les événements de sécurité, eux, doivent y être.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SensitiveDataInLogsTest {

    private static final String EMAIL = "john.doe@test.com";
    private static final String PASSWORD = "StrOng!Passw0rd";
    private static final String NEW_PASSWORD = "N3wStrOng!Passw0rd";
    private static final String WRONG_PASSWORD = "Wr0ng!Passw0rdAttempt";

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private String tokenIn(String link) {
        return link.substring(link.indexOf("token=") + 6);
    }

    @Test
    void fullAccountLifecycle_shouldNeverWriteSecretsOrFullEmailsInLogs() throws Exception {
        try (LogCapture logs = new LogCapture(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)) {
            userService.createUser(new CreateUserRequest("Doe", "John", "1 rue de Test", EMAIL, null, PASSWORD, true));
            ArgumentCaptor<String> activationLink = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendActivationEmail(eq(EMAIL), activationLink.capture());
            String activationToken = tokenIn(activationLink.getValue());
            userService.activateAccount(activationToken);

            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, WRONG_PASSWORD)))
                    .isInstanceOf(UnauthorizedException.class);
            String jwt = authService.login(new LoginRequest(EMAIL, PASSWORD)).token();

            User user = userRepository.findByEmail(EMAIL).orElseThrow();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, List.of()));
            userService.updateOwnAccount(new UpdateUserRequest("Doe", "John", "1 rue de Test", EMAIL, null, NEW_PASSWORD));
            ArgumentCaptor<String> passwordLink = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendPasswordChangeConfirmationEmail(eq(EMAIL), passwordLink.capture());
            String passwordToken = tokenIn(passwordLink.getValue());
            userService.confirmPasswordChange(passwordToken);

            String written = logs.all();

            assertThat(written)
                    .doesNotContain(PASSWORD, NEW_PASSWORD, WRONG_PASSWORD)
                    .doesNotContain(jwt)
                    .doesNotContain(activationToken, passwordToken)
                    .doesNotContain(EMAIL);
        }
    }

    @Test
    void loginAttempts_shouldBeLoggedAsSecurityEvents() throws Exception {
        userService.createUser(new CreateUserRequest("Doe", "John", "1 rue de Test", EMAIL, null, PASSWORD, true));
        ArgumentCaptor<String> activationLink = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendActivationEmail(eq(EMAIL), activationLink.capture());
        userService.activateAccount(tokenIn(activationLink.getValue()));

        try (LogCapture security = new LogCapture("SECURITY")) {
            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, WRONG_PASSWORD)))
                    .isInstanceOf(UnauthorizedException.class);
            authService.login(new LoginRequest(EMAIL, PASSWORD));

            assertThat(security.lines()).hasSize(2);
            assertThat(security.lines().get(0)).contains("CONNEXION refusée", "identifiants invalides", "j***@test.com");
            assertThat(security.lines().get(1)).contains("CONNEXION réussie", "userId=");
        }
    }
}
