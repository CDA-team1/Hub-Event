package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.dto.LegalDocumentRequest;
import fr.CDA.GHE.entity.LegalDocument;
import fr.CDA.GHE.entity.enums.DocumentType;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.mapper.LegalDocumentMapper;
import fr.CDA.GHE.repository.LegalDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Gère les documents légaux de la plateforme (CGU, politique RGPD) — CU31, CU32.
 */
@Service
public class LegalDocumentService {

    private static final Logger log = LoggerFactory.getLogger(LegalDocumentService.class);

    private final LegalDocumentRepository legalDocumentRepository;
    private final LegalDocumentMapper legalDocumentMapper;

    public LegalDocumentService(LegalDocumentRepository legalDocumentRepository,
                                 LegalDocumentMapper legalDocumentMapper) {
        this.legalDocumentRepository = legalDocumentRepository;
        this.legalDocumentMapper = legalDocumentMapper;
    }

    /**
     * Enregistre ou met à jour le document légal d'un type donné (CU31 §2.34, CU32 §2.35).
     * <p>
     * Un seul document par type : s'il n'existe pas encore, il est créé ; sinon son contenu
     * et sa date de mise à jour sont modifiés (règle métier n°3 : la date de mise à jour doit
     * être enregistrée à chaque modification).
     *
     * @param type    type de document à enregistrer ou mettre à jour (RGPD ou CGU)
     * @param request contenu saisi par l'administrateur
     * @return le document enregistré
     * @throws FunctionalException si le contenu est vide
     */
    @Transactional
    public LegalDocumentDto upsert(DocumentType type, LegalDocumentRequest request) throws FunctionalException {
        if (request.content() == null || request.content().isBlank()) {
            throw new FunctionalException("Le contenu du document ne peut pas être vide.");
        }

        LegalDocument document = legalDocumentRepository.findByType(type)
                .orElseGet(() -> new LegalDocument(type, null, null));

        document.setContent(request.content());
        document.setUpdatedAt(LocalDateTime.now());

        LegalDocument saved = legalDocumentRepository.save(document);

        log.info("ENREGISTREMENT document légal : type={} updatedAt={}", saved.getType(), saved.getUpdatedAt());

        return legalDocumentMapper.toDto(saved);
    }
}
