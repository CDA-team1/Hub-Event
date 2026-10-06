package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.RegistrationStatus;

import java.time.LocalDateTime;

/**
 * Représentation d'une inscription exposée par l'API.
 *
 * @param id               identifiant de l'inscription
 * @param eventId          identifiant de l'évènement concerné
 * @param userId           identifiant de l'utilisateur inscrit (REG-04 : nécessaire à
 *                          l'organisateur pour désinscrire un membre, {@code DELETE
 *                          /events/{eventId}/registrations/{userId}})
 * @param userEmail        email de l'utilisateur inscrit
 * @param status           statut de l'inscription (inscrit ou liste d'attente)
 * @param registrationDate date et heure de l'inscription
 */
public record RegistrationDto(
        Long id,
        Long eventId,
        Long userId,
        String userEmail,
        RegistrationStatus status,
        LocalDateTime registrationDate) {
}