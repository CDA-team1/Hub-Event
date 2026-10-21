package fr.CDA.GHE.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données saisies par un utilisateur connecté pour modifier son propre compte
 * (CU13, SFG §2.16).
 * <p>
 * Contrairement à l'inscription (CU5), le mot de passe est facultatif : laissé vide, il
 * n'est pas modifié. Les clubs affiliés ne figurent pas ici : non modifiables depuis cet
 * écran (gérés par l'administrateur).
 *
 * @param lastName      nom
 * @param firstName     prénom
 * @param postalAddress adresse postale
 * @param email         adresse email (doit rester unique si modifiée)
 * @param phone         numéro de téléphone (facultatif)
 * @param password      nouveau mot de passe, ou {@code null}/vide pour ne pas le modifier
 */
public record UpdateUserRequest(
        @NotBlank
        @Size(max = 255)
        String lastName,

        @NotBlank
        @Size(max = 255)
        String firstName,

        @NotBlank
        @Size(max = 255)
        String postalAddress,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 255)
        String phone,

        String password
) {
}
