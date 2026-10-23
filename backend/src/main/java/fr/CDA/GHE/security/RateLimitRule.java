package fr.CDA.GHE.security;

import org.springframework.util.AntPathMatcher;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Quota appliqué à un groupe de routes : {@code capacity} requêtes par {@code window} et par client.
 *
 * @param name     nom de la règle, qui entre dans la clé du seau (un seau par règle et par client)
 * @param methods  méthodes HTTP concernées
 * @param patterns chemins concernés, sans le préfixe {@code /api}, au format Ant ({@code /events/*}{@code /images})
 * @param capacity requêtes autorisées sur la durée
 * @param window   durée du quota
 */
public record RateLimitRule(String name, Set<String> methods, List<String> patterns, int capacity, Duration window) {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    boolean matches(String method, String path) {
        return methods.contains(method) && patterns.stream().anyMatch(pattern -> MATCHER.match(pattern, path));
    }

    /**
     * Quotas par défaut (SEC-09), choisis pour gêner un abus sans gêner un usage normal, ni une
     * démonstration où plusieurs personnes partagent la même adresse IP.
     * <ul>
     *   <li>connexion : essais de mots de passe (force brute) ;</li>
     *   <li>inscription : création de comptes en masse et envois d'emails d'activation ;</li>
     *   <li>jetons des liens reçus par email : devinette du jeton ou du mot de passe temporaire ;</li>
     *   <li>commentaires : spam ;</li>
     *   <li>images : charge sur l'hébergeur externe (quota imgbb) ;</li>
     *   <li>modification de son compte : déclenche l'envoi d'un email lors d'un changement de mot de passe.</li>
     * </ul>
     * Les routes de lecture ne sont pas limitées.
     */
    public static List<RateLimitRule> defaults() {
        return List.of(
                new RateLimitRule("login", Set.of("POST"), List.of("/auth/login"), 10, Duration.ofMinutes(1)),
                new RateLimitRule("signup", Set.of("POST"), List.of("/auth/signup"), 10, Duration.ofMinutes(10)),
                new RateLimitRule("account-token", Set.of("GET", "POST"),
                        List.of("/auth/activate", "/auth/confirm-password-change", "/auth/confirm-account-creation"),
                        10, Duration.ofMinutes(1)),
                new RateLimitRule("comment", Set.of("POST"), List.of("/comments"), 10, Duration.ofMinutes(1)),
                new RateLimitRule("image-upload", Set.of("POST"), List.of("/events/*/images"),
                        10, Duration.ofMinutes(10)),
                new RateLimitRule("account-update", Set.of("PUT"), List.of("/users/me"), 5, Duration.ofMinutes(10))
        );
    }
}
