package fr.CDA.GHE.exception;

/**
 * Exception métier levée lorsqu'une règle fonctionnelle est violée.
 * <p>
 * Retourne un statut HTTP 400 Bad Request via {@link ApiExceptionHandler}.
 * </p>
 */
public class FunctionalException extends Exception{

    /**
     * @param message description de la violation fonctionnelle
     */
    public FunctionalException(String message) {
        super(message);
    }
}
