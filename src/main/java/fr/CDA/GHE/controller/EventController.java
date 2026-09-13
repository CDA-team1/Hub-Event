package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.PageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes évènements (voir {@link ClubController} pour les règles générales).
 * <p>
 * Les GET sont <strong>publics</strong> (liste des publiés, détail, recherche). La recherche
 * ({@code GET /events/search}) est un endpoint distinct, non présent dans {@link EventControllerDoc} :
 * à laisser public quand il sera créé.
 * <p>
 * {@code update}/{@code delete} : en plus du rôle, l'organisateur ne peut agir que sur les évènements
 * de SON club → contrôle de <strong>propriété</strong> à faire dans le service (403 sinon).
 */
@RestController
@RequestMapping("/events")
public class EventController implements EventControllerDoc {

    // Public : liste des évènements publiés
    @Override
    @GetMapping
    public PageDto<EventDto> getAll(Pageable pageable) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Évènement)");
    }

    // Public : détail
    @Override
    @GetMapping("/{id}")
    public EventDto getById(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Évènement)");
    }

    @Override
    @Secured({"ROLE_ORGANIZER", "ROLE_ADMIN"})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(@RequestBody EventDto eventDto) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Évènement)");
    }

    @Override
    @Secured({"ROLE_ORGANIZER", "ROLE_ADMIN"})
    @PutMapping("/{id}")
    public EventDto update(@PathVariable Long id, @RequestBody EventDto eventDto) {
        // TODO SEC-02 : contrôle de propriété (organisateur du club) → 403 si non propriétaire
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Évènement)");
    }

    @Override
    @Secured({"ROLE_ORGANIZER", "ROLE_ADMIN"})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // TODO SEC-02 : contrôle de propriété (organisateur du club) → 403 si non propriétaire
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Évènement)");
    }
}
