package fr.CDA.GHE.entity;

import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests des méthodes métier de l'entité {@link User}.
 */
class UserTest {

    private User newUser() {
        return new User(
                "Dupont", "Jean", "1 rue de Paris", "jean.dupont@test.fr",
                "0600000000", "password", Role.MEMBER
        );
    }

    /**
     * Vérifie qu'activer le compte passe son statut à ACTIVE.
     */
    @Test
    void shouldActivateAccount() {
        User user = newUser();

        user.activate();

        assertEquals(AccountStatus.ACTIVE, user.getStatus());
    }

    /**
     * Vérifie qu'anonymiser le compte passe son statut à ANONYMIZED.
     */
    @Test
    void shouldAnonymizeAccount() {
        User user = newUser();

        user.anonymize();

        assertEquals(AccountStatus.ANONYMIZED, user.getStatus());
    }

    /**
     * Vérifie que le mot de passe de l'utilisateur peut être modifié.
     */
    @Test
    void shouldChangePassword() {
        User user = newUser();

        user.setPassword("newEncodedPassword");

        assertEquals("newEncodedPassword", user.getPassword());
    }

    /**
     * Vérifie qu'une suspension temporaire renseigne la date de fin et le motif.
     */
    @Test
    void shouldSuspendTemporarily() {
        User user = newUser();
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        user.suspendTemporarily(endDate, "Comportement inapproprié");

        assertTrue(user.isSuspended());
        assertEquals(endDate, user.getSuspensionEndDate());
        assertEquals("Comportement inapproprié", user.getSuspensionReason());
    }

    /**
     * Vérifie qu'une suspension indéfinie n'a pas de date de fin.
     */
    @Test
    void shouldSuspendIndefinitely() {
        User user = newUser();

        user.suspendIndefinitely("Fraude avérée");

        assertTrue(user.isSuspended());
        assertNull(user.getSuspensionEndDate());
        assertEquals("Fraude avérée", user.getSuspensionReason());
    }

    /**
     * Vérifie que lever la suspension réinitialise tous les champs liés.
     */
    @Test
    void shouldLiftSuspension() {
        User user = newUser();
        user.suspendTemporarily(LocalDate.of(2026, 12, 31), "Comportement inapproprié");

        user.liftSuspension();

        assertFalse(user.isSuspended());
        assertNull(user.getSuspensionEndDate());
        assertNull(user.getSuspensionReason());
    }
}
