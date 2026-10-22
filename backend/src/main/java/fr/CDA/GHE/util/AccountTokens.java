package fr.CDA.GHE.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Jetons à usage unique des liens envoyés par email (activation de compte, finalisation d'un compte
 * créé par un administrateur, confirmation d'un changement de mot de passe) — SEC-10.
 * <p>
 * Le jeton en clair n'existe que dans l'email : la base ne garde que son empreinte SHA-256. Une fuite
 * de la base ne permet donc pas de reconstituer un lien valide. Une empreinte rapide (SHA-256) suffit,
 * contrairement à un mot de passe, car le jeton est tiré au hasard sur 256 bits : il ne se devine pas
 * et n'a pas besoin d'être « ralenti » contre une attaque par dictionnaire.
 * </p>
 */
public final class AccountTokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private AccountTokens() {
    }

    /**
     * Génère un jeton aléatoire de 256 bits avec un générateur cryptographique.
     *
     * @return le jeton en clair (43 caractères URL-safe), à n'envoyer que dans l'email
     */
    public static String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Calcule l'empreinte stockée en base d'un jeton.
     *
     * @param token jeton en clair, reçu dans le lien ou tout juste généré
     * @return l'empreinte SHA-256 en hexadécimal (64 caractères)
     */
    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponible", exception);
        }
    }
}
