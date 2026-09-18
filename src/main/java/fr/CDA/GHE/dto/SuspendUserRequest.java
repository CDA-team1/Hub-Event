package fr.CDA.GHE.dto;

import java.time.LocalDate;

/**
 * Données saisies par un administrateur pour suspendre un compte utilisateur (SUSP-01).
 * <p>
 * {@code endDate} détermine le type de suspension : {@code null} pour une suspension
 * définitive, une date pour une suspension temporaire jusqu'à cette date incluse.
 * </p>
 *
 * @param reason  motif de la suspension, obligatoire
 * @param endDate date de fin de la suspension, {@code null} si définitive
 */
public record SuspendUserRequest(String reason, LocalDate endDate) {
}