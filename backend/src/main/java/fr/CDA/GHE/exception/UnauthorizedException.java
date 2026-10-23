package fr.CDA.GHE.exception;

/**
 * Exception levée lorsqu'une tentative d'authentification échoue (identifiants invalides,
 * compte non actif, compte suspendu).
 * <p>
 * Retourne un statut HTTP 401 Unauthorized via {@link ApiExceptionHandler}.
 * </p>
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * @param message description de l'échec d'authentification
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
