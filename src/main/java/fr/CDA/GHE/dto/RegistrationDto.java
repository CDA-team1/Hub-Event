package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.RegistrationStatus;

import java.time.LocalDateTime;

/**
 * Représentation d'une inscription exposée par l'API.
 *
 * @param id               identifiant de l'inscription
 * @param eventId          identifiant de l'évènement concerné
 * @param userEmail        email de l'utilisateur inscrit
 * @param status           statut de l'inscription (inscrit ou liste d'attente)
 * @param registrationDate date et heure de l'inscription
 */
public record RegistrationDto(
        Long id,
        Long eventId,
        String userEmail,
        RegistrationStatus status,
        LocalDateTime registrationDate) {
}