package fr.CDA.GHE.dto;

/**
 * Données saisies par un utilisateur connecté pour publier un commentaire (CU12, SFG §2.15).
 *
 * @param eventId identifiant de l'événement commenté
 * @param content contenu du commentaire
 */
public record CreateCommentRequest(Long eventId, String content) {
}
