package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.UpdateUserRequest;
import fr.CDA.GHE.dto.UserDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
