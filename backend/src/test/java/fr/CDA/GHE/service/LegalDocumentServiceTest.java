package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.dto.LegalDocumentRequest;
import fr.CDA.GHE.entity.LegalDocument;
import fr.CDA.GHE.entity.enums.DocumentType;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.repository.LegalDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Tests de {@link LegalDocumentService} avec une vraie base H2 (profil "test", voir
 * {@code GheApplicationTests}) : vérifie le comportement d'upsert (création vs mise à jour,
 * sans doublon) directement contre la contrainte d'unicité par type, plutôt qu'un repository
 * mocké — la logique testée ici dépend justement de ce que le repository fait réellement.
 * <p>
 * {@code @DataJpaTest} n'est pas encore disponible pour cette version de Spring Boot (module
 * non résolu en offline) : on charge donc tout le contexte, comme {@code GheApplicationTests}.
 * {@code @Transactional} annule les écritures après chaque test.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class LegalDocumentServiceTest {

    @Autowired
    private LegalDocumentService legalDocumentService;

    @Autowired
    private LegalDocumentRepository legalDocumentRepository;

    @Test
    void upsert_shouldCreateDocument_whenNoneExistsForType() throws FunctionalException {
        LegalDocumentDto dto = legalDocumentService.upsert(DocumentType.RGPD, new LegalDocumentRequest("Contenu RGPD"));

        assertThat(dto.type()).isEqualTo(DocumentType.RGPD);
        assertThat(dto.content()).isEqualTo("Contenu RGPD");
        assertThat(legalDocumentRepository.findByType(DocumentType.RGPD)).isPresent();
    }

    @Test
    void upsert_shouldUpdateExistingDocument_withoutCreatingDuplicate() throws FunctionalException {
        legalDocumentService.upsert(DocumentType.CGU, new LegalDocumentRequest("Ancien contenu"));

        LegalDocumentDto updated = legalDocumentService.upsert(DocumentType.CGU, new LegalDocumentRequest("Nouveau contenu"));

        assertThat(updated.content()).isEqualTo("Nouveau contenu");
        assertThat(legalDocumentRepository.findAll())
                .filteredOn(document -> document.getType() == DocumentType.CGU)
                .hasSize(1);
    }

    @Test
    void upsert_shouldThrow_whenContentIsBlank() {
        assertThatThrownBy(() -> legalDocumentService.upsert(DocumentType.RGPD, new LegalDocumentRequest("   ")))
                .isInstanceOf(FunctionalException.class);

        assertThat(legalDocumentRepository.findByType(DocumentType.RGPD)).isEmpty();
    }

    @Test
    void extractByType_shouldReturnDocument_whenItExists() {
        legalDocumentRepository.save(
                new LegalDocument(DocumentType.RGPD, "Contenu RGPD", LocalDateTime.now())
        );

        Optional<LegalDocumentDto> result =
                legalDocumentService.extractByType(DocumentType.RGPD);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().content()).isEqualTo("Contenu RGPD");
    }

    @Test
    void extractByType_shouldReturnEmpty_whenDocumentDoesNotExist() {
        Optional<LegalDocumentDto> result =
                legalDocumentService.extractByType(DocumentType.CGU);

        assertThat(result).isEmpty();
    }

    @Test
    void generatePdf_shouldReturnValidPdf_whenDocumentExists() {
        legalDocumentRepository.save(new LegalDocument(DocumentType.RGPD, "Contenu RGPD", LocalDateTime.now()));

        byte[] pdf = legalDocumentService.generatePdf(DocumentType.RGPD);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generatePdf_shouldThrow_whenDocumentDoesNotExist() {
        assertThatThrownBy(() -> legalDocumentService.generatePdf(DocumentType.RGPD))
                .isInstanceOf(NotFoundException.class);
    }
}
