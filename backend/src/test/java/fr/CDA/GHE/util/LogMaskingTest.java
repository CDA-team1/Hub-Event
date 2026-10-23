package fr.CDA.GHE.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de {@link LogMasking} (SEC-14).
 */
class LogMaskingTest {

    @Test
    void maskEmail_shouldKeepOnlyTheFirstLetterAndTheDomain() {
        assertThat(LogMasking.maskEmail("john.doe@test.com")).isEqualTo("j***@test.com");
    }

    @Test
    void maskEmail_shouldNeverRepeatAValueThatIsNotAnEmail() {
        // Une personne qui tape son mot de passe dans le champ email ne doit pas le retrouver dans les logs.
        assertThat(LogMasking.maskEmail("MonMotDePasse123!")).isEqualTo("***");
        assertThat(LogMasking.maskEmail("@test.com")).isEqualTo("***");
        assertThat(LogMasking.maskEmail("john@")).isEqualTo("***");
        assertThat(LogMasking.maskEmail("a@b@c.com")).isEqualTo("***");
        assertThat(LogMasking.maskEmail("")).isEqualTo("***");
        assertThat(LogMasking.maskEmail(null)).isEqualTo("***");
    }
}
