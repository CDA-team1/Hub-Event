package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * Représentation d'une demande d'anonymisation exposée par l'API.
 * <p>
 * Inclut les informations de l'utilisateur concerné (CU28, SFG §2.31 : nom, email, adresse,
 * téléphone) afin de permettre son traitement par un administrateur.
 *
 * @param id          identifiant de la demande
 * @param user        informations de l'utilisateur concerné
 * @param status      statut de la demande ({@link RequestStatus#PENDING} ou {@link RequestStatus#VALIDATED})
 * @param requestDate date et heure de la demande
 */
public record AnonymizationDto(
        Long id,
        UserDto user,
        RequestStatus status,
        LocalDateTime requestDate) {
}
