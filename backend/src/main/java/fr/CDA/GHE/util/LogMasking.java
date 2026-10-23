package fr.CDA.GHE.util;

/**
 * Masquage des données personnelles avant de les écrire dans un journal (SEC-14).
 * <p>
 * Un journal est conservé et lu bien plus largement que la base : on y évite les adresses email
 * complètes. L'anonymisation d'un compte (RGPD) efface la base, pas les fichiers de logs.
 * </p>
 */
public final class LogMasking {

    private LogMasking() {
    }

    /**
     * Masque la partie locale d'une adresse email : {@code john.doe@test.com} devient {@code j***@test.com}.
     * <p>
     * Une valeur qui n'a pas la forme d'un email n'est jamais reprise telle quelle : une personne qui
     * saisit son mot de passe dans le champ email ne doit pas le retrouver dans les logs.
     * </p>
     *
     * @param email valeur saisie ou stockée, éventuellement {@code null}
     * @return l'email masqué, ou {@code ***} si la valeur n'a pas la forme d'un email
     */
    public static String maskEmail(String email) {
        if (email == null) {
            return "***";
        }

        int at = email.indexOf('@');
        if (at < 1 || at == email.length() - 1 || email.indexOf('@', at + 1) >= 0) {
            return "***";
        }

        return email.charAt(0) + "***" + email.substring(at);
    }
}
