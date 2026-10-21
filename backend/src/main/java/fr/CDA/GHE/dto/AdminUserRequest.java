package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Données saisies par un administrateur pour créer ou modifier un compte
 * (CU25, SFG §2.28 — « Gérer les comptes utilisateur »).
 * <p>
 * Contrairement à {@link CreateUserRequest} (inscription self-service), le rôle et les
 * clubs sont choisis par l'admin, et aucun mot de passe n'est fourni : à la création, un
 * mot de passe temporaire est généré par le système et envoyé par email.
 *
 * @param lastName      nom
 * @param firstName     prénom
 * @param postalAddress adresse postale
 * @param email         adresse email (doit être unique)
 * @param phone         numéro de téléphone (facultatif)
 * @param role          rôle attribué au compte
 * @param clubIds       identifiants des clubs d'affiliation (obligatoire pour un ORGANIZER,
 *                      facultatif pour un MEMBER, ignoré pour un ADMIN)
 */
public record AdminUserRequest(
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

        @NotNull
        Role role,

        List<@Positive Long> clubIds
) {
}
