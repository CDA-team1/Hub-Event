package fr.CDA.GHE.dto;

/**
 * Résumé d'un membre affilié à un club, exposé dans {@link ClubDto}.
 * Ne contient jamais d'informations sensibles (mot de passe, adresse postale, statut, etc.) :
 * {@code GET /clubs} est une route publique.
 *
 * @param id        identifiant du membre
 * @param firstname prénom du membre
 * @param lastname  nom de famille du membre
 * @param email     adresse email du membre
 */
public record MemberSummaryDto(Long id, String firstname, String lastname, String email) {
}
