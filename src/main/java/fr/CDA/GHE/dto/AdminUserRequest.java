package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Role;

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
        String lastName,
        String firstName,
        String postalAddress,
        String email,
        String phone,
        Role role,
        List<Long> clubIds
) {
}
