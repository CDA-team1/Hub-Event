package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.entity.enums.DocumentType;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.security.JwtService;
import fr.CDA.GHE.service.LegalDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de la couche web de {@link LegalDocumentController} (patron census CityControllerTest) :
 * {@code @WebMvcTest} ne charge que la tranche web, {@link LegalDocumentService} est mocké.
 * On vérifie donc uniquement l'URL, le code HTTP et la forme du JSON — la logique métier
 * (lecture, génération PDF) est couverte par {@code LegalDocumentServiceTest}.
 * <p>
 * Routes publiques (CU7/CU8) : pas d'authentification nécessaire dans les requêtes.
 */
@WebMvcTest(LegalDocumentController.class)
class LegalDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LegalDocumentService legalDocumentService;

    // JwtAuthenticationFilter est un @Component : @WebMvcTest le charge. Ses dépendances
    // ne font pas partie de la tranche web, on les simule pour que le contexte démarre.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getByType_shouldReturn200WithDocument_whenItExists() throws Exception {
        LegalDocumentDto dto = new LegalDocumentDto(1L, DocumentType.RGPD, "Contenu RGPD",
                LocalDateTime.of(2026, 1, 1, 10, 0));
        when(legalDocumentService.extractByType(DocumentType.RGPD))
                .thenReturn(Optional.of(dto));

        mockMvc.perform(get("/documents/RGPD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("RGPD"))
                .andExpect(jsonPath("$.content").value("Contenu RGPD"));
    }

    @Test
    void getByType_shouldReturn204_whenDocumentDoesNotExist() throws Exception {
        when(legalDocumentService.extractByType(DocumentType.CGU))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/documents/CGU"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void getByTypePdf_shouldReturn200WithPdfContentType_whenDocumentExists() throws Exception {
        byte[] fakePdf = "%PDF-1.7 fake content".getBytes();
        when(legalDocumentService.generatePdf(DocumentType.RGPD)).thenReturn(fakePdf);

        mockMvc.perform(get("/documents/RGPD/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void getByTypePdf_shouldReturn404_whenDocumentDoesNotExist() throws Exception {
        when(legalDocumentService.generatePdf(DocumentType.CGU))
                .thenThrow(new NotFoundException("Aucun document enregistré pour ce type."));

        mockMvc.perform(get("/documents/CGU/pdf"))
                .andExpect(status().isNotFound());
    }
}
