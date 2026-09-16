package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;

import java.util.List;

/**
 * Représentation d'un utilisateur exposée par l'API. Le mot de passe n'y figure jamais.
 *
 * @param id            identifiant de l'utilisateur
 * @param lastName      nom
 * @param firstName     prénom
 * @param postalAddress adresse postale
 * @param email         adresse email
 * @param phone         numéro de téléphone ({@code null} si non renseigné)
 * @param status        statut du compte
 * @param role          rôle de l'utilisateur
 */
public record UserDto(
        Long id,
        String lastName,
        String firstName,
        String postalAddress,
        String email,
        String phone,
        AccountStatus status,
        Role role
) {
}
