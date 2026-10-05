package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AdminUserRequest;
import fr.CDA.GHE.dto.ClubAffiliationRequest;
import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests du contrôleur d'administration des comptes utilisateurs ({@link UserController}).
 * <p>
 * Contexte complet ({@code @SpringBootTest} + {@code @AutoConfigureMockMvc}), pas
 * {@code @WebMvcTest} : {@code @Secured("ROLE_ADMIN")} repose sur
 * {@code @EnableMethodSecurity}, qui a besoin de la sécurité méthode réellement active pour
 * être vérifié — patron {@code EventControllerTest}/{@code AdminLegalDocumentControllerTest}.
 * {@link UserService} est mocké : la logique métier est couverte par {@code UserServiceTest}.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private static UserDto sampleUser() {
        return new UserDto(1L, "Doe", "John", "1 rue de Test", "john.doe@test.com", null,
                AccountStatus.ACTIVE, Role.MEMBER);
    }

    @Test
    void getAll_shouldReturn200_whenAdmin() throws Exception {
        when(userService.extractAll(any(), any()))
        .thenReturn(new PageDto<>(List.of(sampleUser()), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/admin/users")
                        .with(user("admin@test.fr").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john.doe@test.com"));
    }

    @Test
    void getAll_shouldReturnFilteredUsers_whenRoleProvided() throws Exception {
        UserDto admin = new UserDto(
            2L,
            "Admin",
            "Alice",
            "2 rue de Test",
            "admin@test.com",
            null,
            AccountStatus.ACTIVE,
            Role.ADMIN
    );

         when(userService.extractAll(eq(Role.ADMIN), any()))
            .thenReturn(new PageDto<>(List.of(admin), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/admin/users")
                    .param("role", "ADMIN")
                    .with(user("admin@test.fr").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].email").value("admin@test.com"))
            .andExpect(jsonPath("$.content[0].role").value("ADMIN"));
     }

    @Test
    void getAll_shouldReturn403_whenMember() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(user("member@test.fr").roles("MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_shouldReturn200_whenAdmin() throws Exception {
        when(userService.extractById(1L)).thenReturn(sampleUser());

        mockMvc.perform(get("/admin/users/1")
                        .with(user("admin@test.fr").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@test.com"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(userService.extractById(999L)).thenThrow(new NotFoundException("Utilisateur non trouvé"));

        mockMvc.perform(get("/admin/users/999")
                        .with(user("admin@test.fr").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201_whenAdmin() throws Exception {
        when(userService.createUserByAdmin(any(AdminUserRequest.class))).thenReturn(sampleUser());

        mockMvc.perform(post("/admin/users")
                        .with(user("admin@test.fr").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"John","postalAddress":"1 rue de Test",
                                "email":"john.doe@test.com","role":"MEMBER"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@test.com"));
    }

    @Test
    void create_shouldReturn400_whenEmailAlreadyUsed() throws Exception {
        when(userService.createUserByAdmin(any(AdminUserRequest.class)))
                .thenThrow(new FunctionalException("Cette adresse email est déjà utilisée."));

        mockMvc.perform(post("/admin/users")
                        .with(user("admin@test.fr").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"John","postalAddress":"1 rue de Test",
                                "email":"john.doe@test.com","role":"MEMBER"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"John","postalAddress":"1 rue de Test",
                                "email":"john.doe@test.com","role":"MEMBER"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_shouldReturn200_whenAdmin() throws Exception {
        when(userService.updateUserByAdmin(eq(1L), any(AdminUserRequest.class))).thenReturn(sampleUser());

        mockMvc.perform(put("/admin/users/1")
                        .with(user("admin@test.fr").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"John","postalAddress":"1 rue de Test",
                                "email":"john.doe@test.com","role":"MEMBER"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@test.com"));
    }

    @Test
    void delete_shouldReturn204_whenAdmin() throws Exception {
        mockMvc.perform(delete("/admin/users/1")
                        .with(user("admin@test.fr").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateAffiliations_shouldReturn200_whenAdmin() throws Exception {
        ClubDto club = new ClubDto(1L, "Club de Test", null, "1 rue du Club", "club@test.com",
                "0500000000", null, List.of());
        when(userService.updateMemberAffiliations(eq(1L), any(ClubAffiliationRequest.class)))
                .thenReturn(List.of(club));

        mockMvc.perform(put("/admin/users/1/clubs")
                        .with(user("admin@test.fr").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clubIds\":[1]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Club de Test"));
    }

    @Test
    void updateAffiliations_shouldReturn400_whenTargetIsAdmin() throws Exception {
        when(userService.updateMemberAffiliations(eq(1L), any(ClubAffiliationRequest.class)))
                .thenThrow(new FunctionalException("Cette action ne s'applique pas aux administrateurs."));

        mockMvc.perform(put("/admin/users/1/clubs")
                        .with(user("admin@test.fr").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clubIds\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
