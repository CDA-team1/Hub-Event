package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.RegistrationDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes inscriptions (voir {@link ClubController} pour les règles générales).
 * <p>
 * {@code create} (s'inscrire) est réservé au rôle <strong>MEMBER</strong> (décision SEC-02).
 * {@code getById}/{@code delete} : contrôle de <strong>propriété</strong> (le membre n'agit que sur SES
 * inscriptions ; sinon ADMIN, et ORGANIZER de l'évènement pour la lecture) → 403 sinon.
 */
@RestController
@RequestMapping("/registrations")
public class RegistrationController implements RegistrationControllerDoc {

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping
    public PageDto<RegistrationDto> getAll(Pageable pageable) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Inscription)");
    }

    @Override
    @GetMapping("/{id}")
    public RegistrationDto getById(@PathVariable Long id) {
        // TODO SEC-02 : propriétaire, ORGANIZER de l'évènement ou ADMIN → 403 sinon
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Inscription)");
    }

    @Override
    @Secured("ROLE_MEMBER")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationDto create(@RequestBody RegistrationDto registrationDto) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Inscription)");
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // TODO SEC-02 : propriétaire ou ADMIN → 403 sinon
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Inscription)");
    }
}
