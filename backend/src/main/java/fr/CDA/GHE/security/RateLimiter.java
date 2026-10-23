package fr.CDA.GHE.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Limiteur de débit en mémoire, par « seau à jetons » (token bucket) : chaque clé (une adresse IP
 * ou un utilisateur, pour une règle donnée) dispose d'un seau de {@code capacity} jetons qui se
 * remplit régulièrement sur la durée {@code window}. Chaque requête consomme un jeton ; seau vide,
 * elle est refusée.
 * <p>
 * Comparé à un compteur par fenêtre fixe, le seau évite le double quota à cheval sur deux fenêtres
 * et autorise de petites rafales. L'état est local à l'instance : avec plusieurs instances du back,
 * chacune applique son propre quota (suffisant ici, une seule instance est déployée).
 * </p>
 */
public final class RateLimiter {

    /** Résultat d'une demande : autorisée, ou refusée avec le délai avant le prochain jeton. */
    public record Result(boolean allowed, long retryAfterSeconds) {
    }

    private static final long CLEANUP_INTERVAL_NANOS = Duration.ofMinutes(1).toNanos();

    private final LongSupplier nanoClock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private long lastCleanupNanos;

    public RateLimiter() {
        this(System::nanoTime);
    }

    /** Horloge injectable : les tests la font avancer sans attendre. */
    RateLimiter(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
        this.lastCleanupNanos = nanoClock.getAsLong();
    }

    /**
     * Tente de consommer un jeton.
     *
     * @param key      clé du seau, ex. {@code login:ip:203.0.113.5}
     * @param capacity nombre de requêtes autorisées sur la durée {@code window}
     * @param window   durée sur laquelle le seau se remplit entièrement
     * @return {@code allowed} si la requête passe, sinon le nombre de secondes à attendre
     */
    public Result tryAcquire(String key, int capacity, Duration window) {
        long now = nanoClock.getAsLong();
        cleanUpIfDue(now);

        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(capacity, window, now));
        return bucket.take(now);
    }

    /** Nombre de seaux suivis (pour les tests : vérifie que les clés inactives sont purgées). */
    int trackedKeys() {
        return buckets.size();
    }

    /**
     * Supprime, au plus une fois par minute, les seaux inactifs depuis plus d'une fenêtre : ils
     * seraient de toute façon revenus pleins, donc équivalents à un seau neuf.
     */
    private synchronized void cleanUpIfDue(long now) {
        if (now - lastCleanupNanos < CLEANUP_INTERVAL_NANOS) {
            return;
        }
        lastCleanupNanos = now;
        buckets.values().removeIf(bucket -> bucket.isIdle(now));
    }

    private static final class Bucket {

        private final int capacity;
        private final long windowNanos;
        private double tokens;
        private long lastRefillNanos;

        private Bucket(int capacity, Duration window, long now) {
            this.capacity = capacity;
            this.windowNanos = window.toNanos();
            this.tokens = capacity;
            this.lastRefillNanos = now;
        }

        private synchronized Result take(long now) {
            double refillPerNano = (double) capacity / windowNanos;
            tokens = Math.min(capacity, tokens + (now - lastRefillNanos) * refillPerNano);
            lastRefillNanos = now;

            if (tokens >= 1) {
                tokens -= 1;
                return new Result(true, 0);
            }

            double nanosToWait = (1 - tokens) / refillPerNano;
            return new Result(false, Math.max(1, (long) Math.ceil(nanosToWait / 1_000_000_000d)));
        }

        private synchronized boolean isIdle(long now) {
            return now - lastRefillNanos > windowNanos;
        }
    }
}
