package fr.CDA.GHE.dto;

/**
 * Représentation d'une image de galerie exposée par l'API.
 * <p>
 * Ne porte pas le chemin disque (détail de stockage interne) : le contenu se récupère via
 * {@code GET /events/{eventId}/images/{id}}.
 *
 * @param id      identifiant de l'image
 * @param eventId identifiant de l'événement associé
 */
public record ImageDto(Long id, Long eventId) {
}
