package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.RegistrationDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expose les routes d'inscription et de désinscription des utilisateurs aux évènements.
 * <p>
 * SEC-02 : {@code register} (s'inscrire) est réservé au rôle MEMBER (décision SEC-02).
 * {@code unregister} n'a pas besoin de contrôle de propriété supplémentaire : il n'agit
 * jamais que sur l'inscription de l'utilisateur actuellement connecté.
 * </p>
 */
@RestController
@RequestMapping("/events/{eventId}/registrations")
public class RegistrationController implements RegistrationControllerDoc {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    @Secured("ROLE_MEMBER")
    @PostMapping
    public ResponseEntity<RegistrationDto> register(@PathVariable Long eventId) throws FunctionalException {
        RegistrationDto created = registrationService.register(eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @DeleteMapping("/me")
    public ResponseEntity<Void> unregister(@PathVariable Long eventId) throws FunctionalException {
        registrationService.unregister(eventId);
        return ResponseEntity.noContent().build();
    }
}
