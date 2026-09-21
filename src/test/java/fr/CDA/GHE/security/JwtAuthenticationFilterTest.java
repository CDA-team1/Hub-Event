package fr.CDA.GHE.security;

import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de {@link JwtAuthenticationFilter}, centrés sur la revérification du statut du compte
 * à chaque requête (SEC-12) : un compte suspendu ou anonymisé après l'émission d'un JWT ne doit
 * plus pouvoir être authentifié, sans attendre l'expiration du jeton. Unitaire (pas de contexte
 * Spring) : {@link JwtService} et {@link UserDetailsService} sont mockés.
 */
class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenAccountIsActive() throws Exception {
        User user = activeUser();
        mockValidToken("john.doe@test.com", user);

        filter.doFilterInternal(request(), response(), chainMock());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAccountIsInactive() throws Exception {
        User user = activeUser();
        user.setStatus(AccountStatus.INACTIVE);
        mockValidToken("john.doe@test.com", user);

        filter.doFilterInternal(request(), response(), chainMock());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAccountIsAnonymized() throws Exception {
        User user = activeUser();
        user.setStatus(AccountStatus.ANONYMIZED);
        mockValidToken("john.doe@test.com", user);

        filter.doFilterInternal(request(), response(), chainMock());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAccountSuspendedIndefinitely() throws Exception {
        User user = activeUser();
        user.setSuspended(true);
        user.setSuspensionEndDate(null);
        mockValidToken("john.doe@test.com", user);

        filter.doFilterInternal(request(), response(), chainMock());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenSuspensionEndDateNotYetReached() throws Exception {
        User user = activeUser();
        user.setSuspended(true);
        user.setSuspensionEndDate(LocalDate.now().plusDays(3));
        mockValidToken("john.doe@test.com", user);

        filter.doFilterInternal(request(), response(), chainMock());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenSuspensionEndDateAlreadyPassed() throws Exception {
        User user = activeUser();
        user.setSuspended(true);
        user.setSuspensionEndDate(LocalDate.now().minusDays(1));
        mockValidToken("john.doe@test.com", user);

        filter.doFilterInternal(request(), response(), chainMock());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void doFilterInternal_shouldAlwaysContinueChain_evenWhenAccountNotUsable() throws Exception {
        User user = activeUser();
        user.setStatus(AccountStatus.INACTIVE);
        mockValidToken("john.doe@test.com", user);

        FilterChain chain = chainMock();
        HttpServletRequest request = request();
        HttpServletResponse response = response();

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    private void mockValidToken(String email, User user) {
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn(email);
        when(userDetailsService.loadUserByUsername(eq(email))).thenReturn(user);
    }

    private User activeUser() {
        User user = new User();
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPostalAddress("1 rue de Test");
        user.setEmail("john.doe@test.com");
        user.setPassword("hashed-password");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);
        return user;
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        return request;
    }

    private HttpServletResponse response() {
        return mock(HttpServletResponse.class);
    }

    private FilterChain chainMock() {
        return mock(FilterChain.class);
    }
}
