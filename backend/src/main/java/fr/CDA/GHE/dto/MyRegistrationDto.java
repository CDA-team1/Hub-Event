package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.RegistrationStatus;

/**
 * Inscription de l'utilisateur connecté à un évènement (EVT-14).
 *
 * @param status          statut de l'inscription (REGISTERED, WAITING_LIST…)
 * @param waitingPosition position dans la liste d'attente, {@code null} si l'utilisateur n'y est pas
 */
public record MyRegistrationDto(RegistrationStatus status, Integer waitingPosition) {
}