package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.DocumentType;

import java.time.LocalDateTime;

/**
 * Représentation d'un document légal exposée par l'API.
 *
 * @param id        identifiant du document
 * @param type      type de document (RGPD ou CGU)
 * @param content   contenu du document
 * @param updatedAt date et heure de la dernière mise à jour
 */
public record LegalDocumentDto(
        Long id,
        DocumentType type,
        String content,
        LocalDateTime updatedAt) {
}
