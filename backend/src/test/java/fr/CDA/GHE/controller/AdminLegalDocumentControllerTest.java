package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.dto.LegalDocumentRequest;
import fr.CDA.GHE.entity.enums.DocumentType;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.LegalDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur d'administration des documents légaux ({@link AdminLegalDocumentController}).
 * <p>
 * Contexte complet ({@code @SpringBootTest} + {@code @AutoConfigureMockMvc}), pas
 * {@code @WebMvcTest} : {@code @Secured("ROLE_ADMIN")} repose sur
 * {@code @EnableMethodSecurity}, qui a besoin de la sécurité méthode réellement active pour
 * être vérifié — patron {@code EventControllerTest}. {@link LegalDocumentService} est mocké :
 * la logique métier est couverte par {@code LegalDocumentServiceTest}.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminLegalDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LegalDocumentService legalDocumentService;

    @Test
    void upsert_shouldReturn200_whenCalledByAdmin() throws Exception {
        LegalDocumentDto updated = new LegalDocumentDto(1L, DocumentType.RGPD, "Nouveau contenu",
                LocalDateTime.of(2026, 1, 1, 10, 0));
        when(legalDocumentService.upsert(eq(DocumentType.RGPD), any(LegalDocumentRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/admin/documents/RGPD")
                        .with(user("admin@test.fr").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Nouveau contenu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("RGPD"))
                .andExpect(jsonPath("$.content").value("Nouveau contenu"));

        verify(legalDocumentService).upsert(eq(DocumentType.RGPD), any(LegalDocumentRequest.class));
    }

    @Test
    void upsert_shouldReturn400_whenContentIsInvalid() throws Exception {
        when(legalDocumentService.upsert(eq(DocumentType.CGU), any(LegalDocumentRequest.class)))
                .thenThrow(new FunctionalException("Le contenu du document ne peut pas être vide."));

        mockMvc.perform(put("/admin/documents/CGU")
                        .with(user("admin@test.fr").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Le contenu du document ne peut pas être vide."));
    }

    @Test
    void upsert_shouldReturn403_whenCalledByMember() throws Exception {
        mockMvc.perform(put("/admin/documents/RGPD")
                        .with(user("member@test.fr").roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Nouveau contenu\"}"))
                .andExpect(status().isForbidden());

        verify(legalDocumentService, never()).upsert(any(), any());
    }

    @Test
    void upsert_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/admin/documents/RGPD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Nouveau contenu\"}"))
                .andExpect(status().isUnauthorized());

        verify(legalDocumentService, never()).upsert(any(), any());
    }
}
