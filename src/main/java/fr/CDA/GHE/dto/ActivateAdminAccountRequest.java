package fr.CDA.GHE.dto;

/**
 * Données saisies pour finaliser un compte créé par un administrateur (CU25/CPT-06, CdC §
 * « Validation de la création d'un compte »).
 * <p>
 * Le jeton identifiant le compte concerné n'est pas ici : il est transmis en paramètre de
 * requête (même convention que {@code /auth/activate} et {@code /auth/confirm-password-change}).
 *
 * @param temporaryPassword mot de passe temporaire reçu par email, à vérifier avant activation
 * @param newPassword       nouveau mot de passe définitif choisi par l'utilisateur
 * @param confirmPassword   confirmation du nouveau mot de passe (doit être identique)
 */
public record ActivateAdminAccountRequest(
        String temporaryPassword,
        String newPassword,
        String confirmPassword
) {
}
