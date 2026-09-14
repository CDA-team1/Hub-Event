package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AnonymizationDto;
import fr.CDA.GHE.dto.PageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes demandes d'anonymisation (voir {@link ClubController} pour les règles générales).
 * <p>
 * {@code create} : un utilisateur connecté demande l'anonymisation de SON compte → contrôle de
 * <strong>propriété</strong> dans le service (403 sinon), pas de restriction de rôle.
 * La consultation et la validation ({@code update}) sont réservées à l'ADMIN.
 */
@RestController
@RequestMapping("/anonymization-requests")
public class AnonymizationRequestController implements AnonymizationRequestControllerDoc {

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping
    public PageDto<AnonymizationDto> getAll(Pageable pageable) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Anonymisation)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    public AnonymizationDto getById(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Anonymisation)");
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnonymizationDto create(@RequestBody AnonymizationDto anonymizationDto) {
        // TODO SEC-02 : la demande porte sur le compte courant → 403 sinon
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Anonymisation)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public AnonymizationDto update(@PathVariable Long id, @RequestBody AnonymizationDto anonymizationDto) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Anonymisation)");
    }
}
