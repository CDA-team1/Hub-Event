package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.RegistrationDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expose les routes d'inscription des utilisateurs aux évènements.
 */
@RestController
@RequestMapping("/events/{eventId}/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Inscrit l'utilisateur connecté à l'évènement donné.
     *
     * @param eventId identifiant de l'évènement auquel s'inscrire
     * @return 201 Created avec l'inscription créée (statut REGISTERED ou WAITING_LIST)
     * @throws FunctionalException si le compte n'est pas actif, si l'utilisateur est déjà
     *                             inscrit, ou en cas de chevauchement avec un autre évènement
     */
    @PostMapping
    public ResponseEntity<RegistrationDto> register(@PathVariable Long eventId) throws FunctionalException {
        RegistrationDto created = registrationService.register(eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}