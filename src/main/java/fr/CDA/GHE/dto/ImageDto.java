package fr.CDA.GHE.dto;

/**
 * Représentation d'une image de galerie exposée par l'API.
 *
 * @param id      identifiant de l'image
 * @param eventId identifiant de l'événement associé
 * @param url     lien direct vers l'image, hébergée sur un service externe (imgbb)
 */
public record ImageDto(Long id, Long eventId, String url) {
}
