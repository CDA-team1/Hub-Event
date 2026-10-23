package fr.CDA.GHE.dto;

import java.time.LocalDateTime;

/**
 * Représentation d'un commentaire exposée par l'API.
 * <p>
 * {@code authorDisplayName} est déjà formaté (CU12, règles n°4 et 5) : « Prénom + première
 * lettre du nom » pour un auteur normal, ou « SUPPRIMÉ » si l'auteur a depuis été anonymisé —
 * jamais le nom brut ni l'email de l'auteur.
 *
 * @param id                identifiant du commentaire
 * @param eventId           identifiant de l'événement commenté
 * @param authorDisplayName nom d'affichage de l'auteur, déjà formaté
 * @param content           contenu du commentaire
 * @param createdAt         date et heure de publication
 */
public record CommentDto(
        Long id,
        Long eventId,
        String authorDisplayName,
        String content,
        LocalDateTime createdAt) {
}
