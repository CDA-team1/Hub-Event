package fr.CDA.GHE.dto;

/**
 * Résumé d'un club affilié à un utilisateur.
 * Utilisé pour afficher les affiliations dans la page "Mon compte"
 * sans exposer le détail complet du club.
 *
 * @param id identifiant du club
 * @param name nom du club
 */
public record ClubSummaryDto(
    Long id,
    String name
) {
}