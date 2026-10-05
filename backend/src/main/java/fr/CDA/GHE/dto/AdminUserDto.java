package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;

import java.util.List;

/**
 * Détail d'un compte utilisateur exposé à l'administrateur.
 * Inclut les clubs afin de préremplir le formulaire de modification.
 *
 * @param id            identifiant de l'utilisateur
 * @param lastName      nom
 * @param firstName     prénom
 * @param postalAddress adresse postale
 * @param email         adresse email
 * @param phone         numéro de téléphone
 * @param status        statut du compte
 * @param role          rôle de l'utilisateur
 * @param clubs         clubs auxquels l'utilisateur est affilié
 */
public record AdminUserDto(
        Long id,
        String lastName,
        String firstName,
        String postalAddress,
        String email,
        String phone,
        AccountStatus status,
        Role role,
        List<ClubSummaryDto> clubs
) {
}