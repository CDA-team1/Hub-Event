package fr.CDA.GHE.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données saisies par un visiteur pour créer un compte membre non affilié (CU5, SFG §2.8).
 * <p>
 * Ne porte ni statut ni rôle : ces valeurs sont forcées par le service
 * ({@code INACTIVE} / {@code MEMBER}), jamais choisies par le client.
 *
 * @param lastName      nom
 * @param firstName     prénom
 * @param postalAddress adresse postale
 * @param email         adresse email (doit être unique sur la plateforme)
 * @param phone         numéro de téléphone (facultatif)
 * @param password      mot de passe en clair, tel que saisi (12 car. min + 4 types)
 * @param termsAccepted acceptation des CGU et de la politique RGPD
 */
public record CreateUserRequest(
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

        @NotBlank
        String password,

        @AssertTrue
        boolean termsAccepted
) {
}
