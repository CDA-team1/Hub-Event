package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Role;

/**
 * Réponse renvoyée après une connexion réussie.
 *
 * @param token jeton JWT à utiliser pour authentifier les requêtes suivantes
 * @param role  rôle de l'utilisateur connecté
 */
public record LoginResponse (String token, Role role){
}
