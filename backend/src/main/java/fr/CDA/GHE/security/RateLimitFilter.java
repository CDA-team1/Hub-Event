package fr.CDA.GHE.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Limite le nombre de requêtes sur les routes exposées aux abus (SEC-09) : force brute sur la
 * connexion, création de comptes et commentaires en masse, saturation de l'hébergeur d'images.
 * Au-delà du quota, la requête reçoit {@code 429 Too Many Requests} avec l'en-tête
 * {@code Retry-After}, sans atteindre le contrôleur.
 * <p>
 * Le client est identifié par son compte s'il est authentifié, sinon par son adresse IP. Placé après
 * {@link JwtAuthenticationFilter} dans la chaîne pour connaître l'utilisateur. Derrière un
 * reverse proxy, l'adresse IP réelle n'est correcte que si {@code server.forward-headers-strategy}
 * est activé (voir {@code application.properties}) ; sinon tous les clients partageraient celle du proxy.
 * </p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final boolean enabled;
    private final RateLimiter rateLimiter;
    private final List<RateLimitRule> rules;

    @Autowired
    public RateLimitFilter(@Value("${app.rate-limit.enabled:true}") boolean enabled) {
        this(enabled, new RateLimiter(), RateLimitRule.defaults());
    }

    RateLimitFilter(boolean enabled, RateLimiter rateLimiter, List<RateLimitRule> rules) {
        this.enabled = enabled;
        this.rateLimiter = rateLimiter;
        this.rules = rules;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<RateLimitRule> rule = enabled ? findRule(request) : Optional.empty();

        if (rule.isPresent()) {
            String client = clientKey(request);
            RateLimiter.Result result = rateLimiter.tryAcquire(
                    rule.get().name() + ":" + client, rule.get().capacity(), rule.get().window());

            if (!result.allowed()) {
                log.warn("LIMITE DE REQUÊTES dépassée : règle={} client={}", rule.get().name(), client);
                reject(response, result.retryAfterSeconds());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Optional<RateLimitRule> findRule(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return rules.stream().filter(rule -> rule.matches(request.getMethod(), path)).findFirst();
    }

    private static String clientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return "user:" + authentication.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private static void reject(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("Trop de requêtes, réessayez dans " + retryAfterSeconds + " seconde(s).");
    }
}
