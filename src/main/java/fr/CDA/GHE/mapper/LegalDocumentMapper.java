package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.entity.LegalDocument;
import org.springframework.stereotype.Component;

/**
 * Convertit l'entité {@link LegalDocument} vers son DTO d'exposition.
 */
@Component
public class LegalDocumentMapper {

    public LegalDocumentDto toDto(LegalDocument document) {
        return new LegalDocumentDto(
                document.getId(),
                document.getType(),
                document.getContent(),
                document.getUpdatedAt()
        );
    }
}
