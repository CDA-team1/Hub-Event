package fr.CDA.GHE.security;

import fr.CDA.GHE.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de {@link RateLimitFilter} : quota de 2 requêtes par minute sur {@code POST /auth/login} et
 * sur {@code POST /comments}, horloge simulée, requêtes factices (pas de contexte Spring).
 */
class RateLimitFilterTest {

    private final AtomicLong nanos = new AtomicLong();
    private final List<RateLimitRule> rules = List.of(
            new RateLimitRule("login", Set.of("POST"), List.of("/auth/login"), 2, Duration.ofMinutes(1)),
            new RateLimitRule("comment", Set.of("POST"), List.of("/comments"), 2, Duration.ofMinutes(1)));
    private final RateLimitFilter filter = new RateLimitFilter(true, new RateLimiter(nanos::get), rules);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest request(String method, String uri, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        return request;
    }

    /** Passe la requête dans le filtre et renvoie la réponse ; {@code chain.getRequest()} est nul si le filtre l'a stoppée. */
    private MockHttpServletResponse send(MockHttpServletRequest request, MockFilterChain chain) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    private int status(String method, String uri, String ip) throws Exception {
        return send(request(method, uri, ip), new MockFilterChain()).getStatus();
    }

    private void authenticateAs(String email) {
        User user = new User();
        user.setEmail(email);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @Test
    void shouldAnswer429WithRetryAfter_whenQuotaIsExceeded() throws Exception {
        status("POST", "/auth/login", "10.0.0.1");
        status("POST", "/auth/login", "10.0.0.1");

        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = send(request("POST", "/auth/login", "10.0.0.1"), chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("30");
        assertThat(response.getContentAsString()).contains("Trop de requêtes", "30 seconde");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void shouldLetRequestsThrough_whenUnderQuota() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = send(request("POST", "/auth/login", "10.0.0.1"), chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void shouldGiveEachIpItsOwnQuota() throws Exception {
        status("POST", "/auth/login", "10.0.0.1");
        status("POST", "/auth/login", "10.0.0.1");

        assertThat(status("POST", "/auth/login", "10.0.0.1")).isEqualTo(429);
        assertThat(status("POST", "/auth/login", "10.0.0.2")).isEqualTo(200);
    }

    @Test
    void shouldGiveEachRuleItsOwnQuota() throws Exception {
        status("POST", "/auth/login", "10.0.0.1");
        status("POST", "/auth/login", "10.0.0.1");

        assertThat(status("POST", "/auth/login", "10.0.0.1")).isEqualTo(429);
        assertThat(status("POST", "/comments", "10.0.0.1")).isEqualTo(200);
    }

    @Test
    void shouldNotLimitOtherRoutesOrMethods() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertThat(status("GET", "/events", "10.0.0.1")).isEqualTo(200);
            assertThat(status("GET", "/auth/login", "10.0.0.1")).isEqualTo(200);
        }
    }

    @Test
    void shouldIgnoreTheContextPathWhenMatchingRoutes() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/auth/login", "10.0.0.1");
        request.setContextPath("/api");
        send(request, new MockFilterChain());
        send(request, new MockFilterChain());

        MockHttpServletResponse response = send(request, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void shouldAllowAgainOnceTheQuotaHasRefilled() throws Exception {
        status("POST", "/auth/login", "10.0.0.1");
        status("POST", "/auth/login", "10.0.0.1");
        assertThat(status("POST", "/auth/login", "10.0.0.1")).isEqualTo(429);

        nanos.addAndGet(Duration.ofSeconds(30).toNanos());

        assertThat(status("POST", "/auth/login", "10.0.0.1")).isEqualTo(200);
    }

    @Test
    void shouldLimitAuthenticatedUsersByAccountRatherThanByIp() throws Exception {
        authenticateAs("a@test.com");
        status("POST", "/comments", "10.0.0.1");
        status("POST", "/comments", "10.0.0.1");
        assertThat(status("POST", "/comments", "10.0.0.1")).isEqualTo(429);

        // Un autre compte derrière la même adresse IP n'est pas pénalisé...
        authenticateAs("b@test.com");
        assertThat(status("POST", "/comments", "10.0.0.1")).isEqualTo(200);

        // ... et changer d'adresse IP ne permet pas au premier de contourner son quota.
        authenticateAs("a@test.com");
        assertThat(status("POST", "/comments", "10.0.0.99")).isEqualTo(429);
    }

    @Test
    void shouldNotLimitAnything_whenDisabled() throws Exception {
        RateLimitFilter disabled = new RateLimitFilter(false, new RateLimiter(nanos::get), rules);

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            disabled.doFilter(request("POST", "/auth/login", "10.0.0.1"), response, new MockFilterChain());
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }
}
