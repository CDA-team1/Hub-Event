package fr.CDA.GHE.dto;

/**
 * Corps de la requête d'annulation d'inscription par l'organisateur (REG-04).
 *
 * @param reason motif de l'annulation, obligatoire
 */
public record CancelRegistrationRequest (String reason){
}
