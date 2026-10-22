package fr.CDA.GHE.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Gestionnaire global des exceptions de l'application.
 * <p>
 * Intercepte les exceptions métier et techniques pour retourner
 * des réponses HTTP appropriées au client.
 * </p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

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
     * Gère les paramètres d'URL absents ou impossibles à convertir
     * et retourne un 400 Bad Request.
     *
     * @param ex l'exception liée au paramètre de requête
     * @return la réponse HTTP indiquant que la requête est invalide
     */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    protected ResponseEntity<String> handleRequestParameterException(Exception ex) {
        return ResponseEntity.badRequest().body("Requête invalide");
    }

    /**
     * Gère les {@link ForbiddenException} et retourne un 403 Forbidden.
     *
     * @param ex l'exception levée
     * @return la réponse HTTP avec le message d'erreur
     */
    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<String> handleForbidden(ForbiddenException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * Gère les {@link UnauthorizedException} et retourne un 401 Unauthorized.
     *
     * @param ex l'exception levée
     * @return la réponse HTTP avec le message d'erreur
     */
    @ExceptionHandler(UnauthorizedException.class)
    protected ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
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
     * Gère les envois de fichiers trop volumineux (limites {@code spring.servlet.multipart.*}) et
     * retourne un 400 Bad Request : le front n'affiche le message du back que pour cette erreur.
     *
     * @param ex l'exception levée
     * @return la réponse HTTP avec le message d'erreur
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<String> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest()
                .body("Les images sont trop volumineuses : 5 Mo maximum par fichier et 30 Mo par envoi.");
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

    /**
    * Gère les erreurs de validation des corps de requête et retourne un 400 Bad Request.
    *
    * @param ex l'exception de validation levée
    * @return la réponse HTTP avec le premier message de validation disponible
    */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Requête invalide");

        return ResponseEntity.badRequest().body(message);
    }

    /**
    * Gère les corps de requête JSON illisibles ou incompatibles avec le DTO
    * et retourne un 400 Bad Request.
    *
    * @param ex l'exception de désérialisation levée
    * @return la réponse HTTP indiquant que la requête est invalide
    */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("Requête invalide");
    }

    /**
     * Gère les requêtes que l'API ne sait pas traiter : URL inconnue (404), méthode HTTP non prévue
     * pour cette URL (405) ou type de contenu non supporté (415).
     * <p>
     * Sans ce handler, le filet de sécurité ci-dessous les transformerait en 500 et journaliserait
     * une erreur avec stack trace à chaque URL inconnue testée par un robot ou un scanner.
     * </p>
     *
     * @param ex l'exception levée par Spring MVC (elle porte son propre code HTTP)
     * @return la réponse HTTP avec le code d'origine, sans détail technique
     */
    @ExceptionHandler({
            NoResourceFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class
    })
    protected ResponseEntity<String> handleUnsupportedRequest(Exception ex) {
        ErrorResponse errorResponse = (ErrorResponse) ex;
        HttpStatusCode status = errorResponse.getStatusCode();
        String message = status.value() == HttpStatus.NOT_FOUND.value()
                ? "Ressource introuvable"
                : "Requête non supportée";

        // Les en-têtes d'origine sont conservés (ex. « Allow » listant les méthodes d'un 405).
        return ResponseEntity.status(status).headers(errorResponse.getHeaders()).body(message);
    }

    /**
     * Filet de sécurité pour toute exception non prévue par un handler plus spécifique.
     * <p>
     * Loggée en ERROR avec la stack trace complète : sans ça, un bug inattendu devient un
     * 500 "Erreur interne" totalement silencieux, sans aucune trace côté serveur.
     *
     * @param ex l'exception levée
     * @return la réponse HTTP générique, sans détail technique exposé au client
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> handleGeneric(Exception ex) {
        log.error("Erreur interne non gérée", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne");
    }
}
