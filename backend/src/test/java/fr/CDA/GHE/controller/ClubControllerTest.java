package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.service.ClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur des clubs ({@link ClubController}), centrés sur l'autorisation (CLUB-01,
 * CdC p.10 : "Liste des clubs" est réservée à l'administrateur — aucune route publique ici).
 * <p>
 * Contexte complet ({@code @SpringBootTest} + {@code @AutoConfigureMockMvc}), pas
 * {@code @WebMvcTest} — patron {@code AdminLegalDocumentControllerTest}. {@link ClubService} est
 * mocké : la logique métier est couverte par {@code ClubServiceTest}.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubService clubService;

    @Test
    void getAll_shouldReturn200_whenCalledByAdmin() throws Exception {
        ClubDto club = new ClubDto(1L, "Club de boxe", Category.SPORT, "1 rue de Test",
                "club@test.fr", "0600000000", null, List.of());
        PageDto<ClubDto> page = new PageDto<>(List.of(club), 0, 20, 1, 1, true, true);
        when(clubService.extractAll(any())).thenReturn(page);

        mockMvc.perform(get("/clubs").with(user("admin@test.fr").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Club de boxe"));
    }

    @Test
    void getAll_shouldReturn403_whenCalledByMember() throws Exception {
        mockMvc.perform(get("/clubs").with(user("member@test.fr").roles("MEMBER")))
                .andExpect(status().isForbidden());

        verify(clubService, never()).extractAll(any());
    }

    @Test
    void getAll_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/clubs"))
                .andExpect(status().isUnauthorized());

        verify(clubService, never()).extractAll(any());
    }

    @Test
    void getMine_shouldReturn200_whenCalledByOrganizer() throws Exception {
        ClubDto club = new ClubDto(1L, "Club de boxe", Category.SPORT, "1 rue de Test",
                "club@test.fr", "0600000000", null, List.of());
        when(clubService.extractMine()).thenReturn(List.of(club));

        mockMvc.perform(get("/clubs/mine").with(user("organizer@test.fr").roles("ORGANIZER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Club de boxe"));
    }

    @Test
    void getMine_shouldReturn403_whenCalledByAdmin() throws Exception {
        mockMvc.perform(get("/clubs/mine").with(user("admin@test.fr").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verify(clubService, never()).extractMine();
    }

    @Test
    void getMine_shouldReturn403_whenCalledByMember() throws Exception {
        mockMvc.perform(get("/clubs/mine").with(user("member@test.fr").roles("MEMBER")))
                .andExpect(status().isForbidden());

        verify(clubService, never()).extractMine();
    }

    @Test
    void getMine_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/clubs/mine"))
                .andExpect(status().isUnauthorized());

        verify(clubService, never()).extractMine();
    }
}
