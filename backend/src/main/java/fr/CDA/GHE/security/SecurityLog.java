package fr.CDA.GHE.security;

import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.util.LogMasking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Journal des événements de sécurité (SEC-14) : connexions réussies et refusées, jetons refusés,
 * accès interdits, quotas dépassés.
 * <p>
 * Tous passent par un journal nommé {@code SECURITY}, avec le même format ({@code ÉVÉNEMENT : clé=valeur}),
 * pour pouvoir les filtrer ou les envoyer ailleurs sans toucher au reste des logs.
 * </p>
 * <p>
 * <b>Règle :</b> on n'écrit jamais ici un mot de passe, un JWT, un jeton de lien email ni une
 * adresse email complète (voir {@link LogMasking}), et jamais la query string d'une URL (elle porte
 * les jetons des liens d'activation).
 * </p>
 */
public final class SecurityLog {

    private static final Logger LOG = LoggerFactory.getLogger("SECURITY");

    private SecurityLog() {
    }

    public static void loginSucceeded(Long userId) {
        LOG.info("CONNEXION réussie : userId={} ip={}", userId, clientIp());
    }

    public static void loginFailed(String email, String reason) {
        LOG.warn("CONNEXION refusée : raison={} email={} ip={}", reason, LogMasking.maskEmail(email), clientIp());
    }

    /** Jeton JWT valide mais compte inutilisable (introuvable, inactif, suspendu). */
    public static void tokenRefused(String reason, Long userId) {
        LOG.info("JETON refusé : raison={} userId={} ip={}", reason, userId, clientIp());
    }

    public static void accessDenied(String method, String path) {
        LOG.warn("ACCÈS REFUSÉ : {} {} {} ip={}", method, path, currentUser(), clientIp());
    }

    public static void rateLimited(String rule, String ip) {
        LOG.warn("LIMITE DE REQUÊTES dépassée : règle={} ip={}", rule, ip);
    }

    private static String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return "userId=" + user.getId();
        }
        return "anonyme";
    }

    private static String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getRemoteAddr();
        }
        return "inconnue";
    }
}
