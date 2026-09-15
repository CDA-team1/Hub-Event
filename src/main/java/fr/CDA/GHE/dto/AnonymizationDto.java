package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * Représentation d'une demande d'anonymisation exposée par l'API.
 *
 * @param id          identifiant de la demande
 * @param userEmail   email de l'utilisateur concerné
 * @param status      statut de la demande ({@link RequestStatus#PENDING} ou {@link RequestStatus#VALIDATED})
 * @param requestDate date et heure de la demande
 */
public record AnonymizationDto(
        Long id,
        String userEmail,
        RequestStatus status,
        LocalDateTime requestDate) {
}
