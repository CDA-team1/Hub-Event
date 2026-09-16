package fr.CDA.GHE.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global des exceptions de l'application.
 * <p>
 * Intercepte les exceptions métier et techniques pour retourner
 * des réponses HTTP appropriées au client.
 * </p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Gère les {@link NotFoundException} et retourne un 404 Not Found.
     *
     * @param ex l'exception levée
     * @return la réponse HTTP avec le message d'erreur
     */
    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<String> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Gère les {@link FunctionalException} et retourne un 400 Bad Request.
     *
     * @param ex l'exception levée
     * @return la réponse HTTP avec le message d'erreur
     */
    @ExceptionHandler(FunctionalException.class)
    protected ResponseEntity<String> handleFunctionalException(FunctionalException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

  /**
   * Gère les refus d'accès et retourne un 403 Forbidden.
   *
   * @param ex l'exception d'autorisation levée
   * @return la réponse HTTP indiquant que l'accès est refusé
   */
  @ExceptionHandler(AccessDeniedException.class)
  protected ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé");
  }
  
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne");
    }
}
