package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.dto.PageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes documents légaux (voir {@link ClubController} pour les règles générales).
 * <p>
 * La gestion (CRUD) est réservée à l'ADMIN. La consultation publique se fait par TYPE
 * ({@code GET /documents/{type}}, type ∈ {GDPR, TERMS}) — endpoint distinct non présent dans
 * {@link LegalDocumentControllerDoc}. ⚠️ Attention à la collision de chemin avec {@code GET /documents/{id}}
 * (numérique) ci-dessous : prévoir un chemin dédié (ex. {@code /documents/type/{type}}) lors de sa création.
 */
@RestController
@RequestMapping("/documents")
public class LegalDocumentController implements LegalDocumentControllerDoc {

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping
    public PageDto<LegalDocumentDto> getAll(Pageable pageable) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Document légal)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    public LegalDocumentDto getById(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Document légal)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LegalDocumentDto create(@RequestBody LegalDocumentDto legalDocumentDto) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Document légal)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public LegalDocumentDto update(@PathVariable Long id, @RequestBody LegalDocumentDto legalDocumentDto) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Document légal)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Document légal)");
    }
}
