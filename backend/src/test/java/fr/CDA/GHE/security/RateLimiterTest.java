package fr.CDA.GHE.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de {@link RateLimiter} avec une horloge simulée : on fait « passer le temps » sans attendre.
 */
class RateLimiterTest {

    private final AtomicLong nanos = new AtomicLong();
    private final RateLimiter limiter = new RateLimiter(nanos::get);

    private static final Duration MINUTE = Duration.ofMinutes(1);

    private void advance(Duration duration) {
        nanos.addAndGet(duration.toNanos());
    }

    @Test
    void tryAcquire_shouldAllowUpToCapacity_thenRefuse() {
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryAcquire("login:ip:1", 3, MINUTE).allowed()).isTrue();
        }

        assertThat(limiter.tryAcquire("login:ip:1", 3, MINUTE).allowed()).isFalse();
    }

    @Test
    void tryAcquire_shouldReportHowLongToWait() {
        for (int i = 0; i < 3; i++) {
            limiter.tryAcquire("login:ip:1", 3, MINUTE);
        }

        RateLimiter.Result refused = limiter.tryAcquire("login:ip:1", 3, MINUTE);

        // 3 requêtes par minute = 1 jeton toutes les 20 secondes
        assertThat(refused.retryAfterSeconds()).isEqualTo(20);
    }

    @Test
    void tryAcquire_shouldRefillOverTime() {
        for (int i = 0; i < 3; i++) {
            limiter.tryAcquire("login:ip:1", 3, MINUTE);
        }

        advance(Duration.ofSeconds(20));

        assertThat(limiter.tryAcquire("login:ip:1", 3, MINUTE).allowed()).isTrue();
        assertThat(limiter.tryAcquire("login:ip:1", 3, MINUTE).allowed()).isFalse();
    }

    @Test
    void tryAcquire_shouldNeverExceedCapacityAfterALongPause() {
        limiter.tryAcquire("login:ip:1", 3, MINUTE);
        advance(Duration.ofHours(5));

        int allowed = 0;
        for (int i = 0; i < 10; i++) {
            if (limiter.tryAcquire("login:ip:1", 3, MINUTE).allowed()) {
                allowed++;
            }
        }

        assertThat(allowed).isEqualTo(3);
    }

    @Test
    void tryAcquire_shouldKeepKeysIndependent() {
        for (int i = 0; i < 3; i++) {
            limiter.tryAcquire("login:ip:1", 3, MINUTE);
        }

        assertThat(limiter.tryAcquire("login:ip:1", 3, MINUTE).allowed()).isFalse();
        assertThat(limiter.tryAcquire("login:ip:2", 3, MINUTE).allowed()).isTrue();
        assertThat(limiter.tryAcquire("signup:ip:1", 3, MINUTE).allowed()).isTrue();
    }

    @Test
    void tryAcquire_shouldForgetIdleKeys() {
        limiter.tryAcquire("login:ip:1", 3, MINUTE);
        limiter.tryAcquire("login:ip:2", 3, MINUTE);
        assertThat(limiter.trackedKeys()).isEqualTo(2);

        advance(Duration.ofMinutes(5));
        limiter.tryAcquire("login:ip:3", 3, MINUTE);

        assertThat(limiter.trackedKeys()).isEqualTo(1);
    }
}
