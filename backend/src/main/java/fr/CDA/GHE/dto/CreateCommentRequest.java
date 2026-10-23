package fr.CDA.GHE.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Données saisies par un utilisateur connecté pour publier un commentaire (CU12, SFG §2.15).
 *
 * @param eventId identifiant de l'événement commenté
 * @param content contenu du commentaire
 */
public record CreateCommentRequest(
        @NotNull
        @Positive
        Long eventId,

        @NotBlank
        String content
) {
}
