package fr.CDA.GHE.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Donne l'email de l'utilisateur authentifié à l'origine de la requête en cours.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * @return l'email de l'utilisateur actuellement authentifié
     * @throws IllegalStateException si personne n'est authentifié
     */
    public static String email() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Aucun utilisateur authentifié");
        }
        return authentication.getName();
    }
}