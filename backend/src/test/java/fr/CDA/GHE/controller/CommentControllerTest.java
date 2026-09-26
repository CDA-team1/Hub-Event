package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.service.CommentService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur des commentaires ({@link CommentController}).
 * <p>
 * Contexte complet ({@code @SpringBootTest} + {@code @AutoConfigureMockMvc}), comme
 * {@code UserProfileControllerTest} : pas de {@code @Secured} ici, mais on vérifie quand même
 * le 401 sans authentification, ce qui a besoin de la même chaîne de sécurité active.
 * {@link CommentService} est mocké : la logique métier est couverte par {@code CommentServiceTest}.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Test
    void create_shouldReturn201_whenAuthenticated() throws Exception {
        CommentDto created = new CommentDto(1L, 100L, "John D.",
                "Super évènement !", LocalDateTime.of(2026, 9, 17, 10, 0));
        when(commentService.createComment(any())).thenReturn(created);

        mockMvc.perform(post("/comments")
                        .with(user("john.doe@test.fr").roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":100,\"content\":\"Super évènement !\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorDisplayName").value("John D."))
                .andExpect(jsonPath("$.content").value("Super évènement !"));
    }

    @Test
    void create_shouldReturn400_whenContentIsBlank() throws Exception {
        when(commentService.createComment(any()))
                .thenThrow(new FunctionalException("Veuillez saisir un commentaire."));

        mockMvc.perform(post("/comments")
                        .with(user("john.doe@test.fr").roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":100,\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Veuillez saisir un commentaire."));
    }

    @Test
    void create_shouldReturn404_whenEventDoesNotExist() throws Exception {
        when(commentService.createComment(any()))
                .thenThrow(new NotFoundException("Événement introuvable"));

        mockMvc.perform(post("/comments")
                        .with(user("john.doe@test.fr").roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":999,\"content\":\"Super évènement !\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":100,\"content\":\"Super évènement !\"}"))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).createComment(any());
    }
}
