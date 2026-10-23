package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.entity.enums.DocumentType;
import fr.CDA.GHE.service.LegalDocumentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SEC-02 — Autorisations des routes de consultation des documents légaux.
 * <p>
 * Routes publiques (CU7 : consulter la politique RGPD, CU8 : consulter les CGU), accessibles
 * sans authentification. La saisie/mise à jour est une route distincte et réservée à l'admin :
 * {@code PUT /admin/documents/{type}}, voir {@link AdminLegalDocumentController}.
 */
@RestController
@RequestMapping("/documents")
public class LegalDocumentController implements LegalDocumentControllerDoc {

    private final LegalDocumentService legalDocumentService;

    public LegalDocumentController(LegalDocumentService legalDocumentService) {
        this.legalDocumentService = legalDocumentService;
    }

    @Override
    @GetMapping("/{type}")
    public ResponseEntity<LegalDocumentDto> getByType(@PathVariable DocumentType type) {
        return legalDocumentService.extractByType(type)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @GetMapping(value = "/{type}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getByTypePdf(@PathVariable DocumentType type) {
        byte[] pdf = legalDocumentService.generatePdf(type);

        ContentDisposition contentDisposition = ContentDisposition.inline()
                .filename(type.name().toLowerCase() + ".pdf")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(pdf);
    }
}
