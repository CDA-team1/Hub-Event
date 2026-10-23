package fr.CDA.GHE.dto;

/**
 * Résumé d'un membre affilié à un club, exposé dans {@link ClubDto}.
 * Ne contient jamais d'informations sensibles (mot de passe, adresse postale, statut, etc.) :
 * {@link fr.CDA.GHE.controller.ClubController} n'en a pas besoin pour gérer les clubs.
 *
 * @param id        identifiant du membre
 * @param firstName prénom du membre
 * @param lastName  nom de famille du membre
 * @param email     adresse email du membre
 */
public record MemberSummaryDto(Long id, String firstName, String lastName, String email) {
}
