package fr.CDA.GHE.dto;

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
 */
public record CreateUserRequest(
        String lastName,
        String firstName,
        String postalAddress,
        String email,
        String phone,
        String password
) {
}
