package fr.CDA.GHE.dto;

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
        String lastName,
        String firstName,
        String postalAddress,
        String email,
        String phone,
        String password
) {
}
