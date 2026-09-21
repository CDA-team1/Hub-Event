package fr.CDA.GHE.util;

import fr.CDA.GHE.entity.User;

import java.util.UUID;

/**
 * Génère des valeurs aléatoires pour remplacer les données personnelles d'un utilisateur
 * anonymisé (CU29, SFG §2.32 ; CdC : « toutes les informations de la personne concernée sont
 * remplacées par des caractères aléatoires »).
 */
public final class AnonymizationUtils {

    private AnonymizationUtils() {
    }

    /**
     * Remplace les données personnelles de l'utilisateur par des valeurs aléatoires, uniques
     * et sans rapport avec son identité réelle. Ne modifie ni son identifiant, ni son rôle, ni
     * son statut, ni son mot de passe : ces responsabilités restent au service appelant.
     *
     * @param user utilisateur dont les données doivent être anonymisées
     */
    public static void anonymize(User user) {
        user.setLastName(randomToken());
        user.setFirstName(randomToken());
        user.setPostalAddress(randomToken());
        user.setEmail("anonymized-" + randomToken() + "@ghe.local");
        user.setPhone(null);
    }

    private static String randomToken() {
        return UUID.randomUUID().toString();
    }
}
