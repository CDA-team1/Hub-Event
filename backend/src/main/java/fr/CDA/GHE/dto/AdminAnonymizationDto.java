package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * Représentation d'une demande d'anonymisation exposée à l'administrateur.
 * Elle inclut les informations administratives de l'utilisateur, notamment ses clubs affiliés.
 *
 * @param id          identifiant de la demande
 * @param user        informations administratives de l'utilisateur concerné
 * @param status      statut de la demande
 * @param requestDate date et heure de la demande
 */
public record AdminAnonymizationDto(
        Long id,
        AdminUserDto user,
        RequestStatus status,
        LocalDateTime requestDate
) {
}