package fr.CDA.GHE.dto;

/**
 * Données saisies par l'utilisateur pour se connecter à son compte.
 *
 * @param email    adresse email du compte
 * @param password mot de passe en clair, tel que saisi par l'utilisateur
 */
public record LoginRequest (String email, String password){
}
