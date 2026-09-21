package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.LegalDocument;
import fr.CDA.GHE.entity.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link LegalDocument}.
 */
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

    /**
     * Recherche le document légal d'un type donné (au plus un par type).
     *
     * @param type le type de document recherché (RGPD ou CGU)
     * @return le document correspondant, s'il existe
     */
    Optional<LegalDocument> findByType(DocumentType type);
}
