package fr.CDA.GHE.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Données saisies par l'utilisateur pour se connecter à son compte.
 *
 * @param email    adresse email du compte
 * @param password mot de passe en clair, tel que saisi par l'utilisateur
 */
public record LoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {
}
