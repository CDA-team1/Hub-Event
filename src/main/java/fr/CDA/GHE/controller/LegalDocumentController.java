package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.dto.LegalDocumentRequest;
import fr.CDA.GHE.entity.enums.DocumentType;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.LegalDocumentService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SEC-02 — Autorisations des routes documents légaux.
 * <p>
 * Réservé à l'admin (CU31 : saisie/MAJ de la politique RGPD, CU32 : saisie/MAJ des CGU). Un
 * seul document par type, adressé par type plutôt que par id numérique. La consultation
 * publique (CU7/CU8) est une route distincte, pas encore implémentée.
 */
@RestController
@RequestMapping("/admin/documents")
public class LegalDocumentController implements LegalDocumentControllerDoc {

    private final LegalDocumentService legalDocumentService;

    public LegalDocumentController(LegalDocumentService legalDocumentService) {
        this.legalDocumentService = legalDocumentService;
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PutMapping("/{type}")
    public LegalDocumentDto upsert(@PathVariable DocumentType type, @RequestBody LegalDocumentRequest request)
            throws FunctionalException {
        return legalDocumentService.upsert(type, request);
    }
}
