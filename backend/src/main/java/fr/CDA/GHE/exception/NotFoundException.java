package fr.CDA.GHE.exception;

/**
 * Exception levée lorsqu'une ressource demandée est introuvable.
 * <p>
 * Retourne un statut HTTP 404 Not Found via {@link ApiExceptionHandler}.
 * </p>
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param message description de la ressource introuvable
     */
    public NotFoundException(String message) {
        super(message);
    }
}
