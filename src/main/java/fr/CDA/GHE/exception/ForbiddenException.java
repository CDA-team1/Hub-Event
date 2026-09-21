package fr.CDA.GHE.exception;

/**
 * Exception levée lorsqu'un utilisateur authentifié n'a pas les droits nécessaires
 * pour effectuer une action (ex : contrôle de propriété).
 * <p>
 * Retourne un statut HTTP 403 Forbidden via {@link ApiExceptionHandler}.
 * </p>
 */
public class ForbiddenException extends RuntimeException {

    /**
     * @param message description de l'accès refusé
     */
    public ForbiddenException(String message){
        super(message);
    }
}
