package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.UpdateUserRequest;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.dto.ClubSummaryDto;
import fr.CDA.GHE.dto.UserProfileDto;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Tests du contrôleur du profil de l'utilisateur connecté ({@link UserProfileController}).
 * <p>
 * Contexte complet ({@code @SpringBootTest} + {@code @AutoConfigureMockMvc}), comme
 * {@code UserControllerTest} : pas de {@code @Secured} ici, mais on vérifie quand même le
 * 401 sans authentification, ce qui a besoin de la même chaîne de sécurité active.
 * {@link UserService} est mocké : la logique métier est couverte par {@code UserServiceTest}.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getOwnAccount_shouldReturn200_whenAuthenticated() throws Exception {
        UserProfileDto profile = new UserProfileDto(
                1L,
                "Doe",
                "John",
                "1 rue de Test",
                "john.doe@test.com",
                "0600000000",
                AccountStatus.ACTIVE,
                Role.MEMBER,
                List.of(new ClubSummaryDto(1L, "Club de Test"))
        );

        when(userService.getOwnAccount()).thenReturn(profile);

        mockMvc.perform(get("/users/me")
                        .with(user("john.doe@test.com").roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@test.com"))
                .andExpect(jsonPath("$.clubs[0].id").value(1))
                .andExpect(jsonPath("$.clubs[0].name").value("Club de Test"));
    }

    @Test
    void getOwnAccount_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_shouldReturn200_whenAuthenticated() throws Exception {
        UserDto updated = new UserDto(1L, "Doe", "Jonathan", "Nouvelle adresse",
                "john.doe@test.com", null, AccountStatus.ACTIVE, Role.MEMBER);
        when(userService.updateOwnAccount(any(UpdateUserRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/users/me")
                        .with(user("john.doe@test.com").roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"Jonathan","postalAddress":"Nouvelle adresse",
                                "email":"john.doe@test.com"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jonathan"));
    }

    @Test
    void update_shouldReturn400_whenEmailAlreadyUsed() throws Exception {
        when(userService.updateOwnAccount(any(UpdateUserRequest.class)))
                .thenThrow(new FunctionalException("Cette adresse email est déjà utilisée."));

        mockMvc.perform(put("/users/me")
                        .with(user("john.doe@test.com").roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"John","postalAddress":"1 rue de Test",
                                "email":"taken@test.com"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cette adresse email est déjà utilisée."));
    }

    @Test
    void update_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"John","postalAddress":"1 rue de Test",
                                "email":"john.doe@test.com"}"""))
                .andExpect(status().isUnauthorized());
    }
}
