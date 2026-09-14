package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.security.JwtService;
import fr.CDA.GHE.service.AuthService;
import fr.CDA.GHE.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de la couche web de {@link AuthController} (patron census CityControllerTest) :
 * {@code @WebMvcTest} ne charge que la tranche web, {@link AuthService} est mocké.
 * On vérifie donc uniquement l'URL, le code HTTP et la forme du JSON — la logique
 * métier du login est couverte par {@code AuthServiceTest}.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    // JwtAuthenticationFilter est un @Component : @WebMvcTest le charge. Ses dépendances
    // ne font pas partie de la tranche web, on les simule pour que le contexte démarre.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void login_shouldReturn200WithToken_whenCredentialsAreValid() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("fake-jwt-token", Role.MEMBER));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john.doe@test.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void login_shouldReturn400_whenCredentialsAreInvalid() throws Exception {
        when(authService.login(any()))
                .thenThrow(new FunctionalException("Email ou mot de passe invalide"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john.doe@test.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email ou mot de passe invalide"));
    }

    @Test
    void login_shouldReturn400_whenAccountNotActive() throws Exception {
        when(authService.login(any()))
                .thenThrow(new FunctionalException("Compte inactif ou anonymisé, connexion impossible"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john.doe@test.com\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Compte inactif ou anonymisé, connexion impossible"));
    }

    @Test
    void signup_shouldReturn201_whenRequestIsValid() throws Exception {
        UserDto created = new UserDto(1L, "Doe", "John", "1 rue de Test",
                "john.doe@test.com", null, AccountStatus.INACTIVE, Role.MEMBER);
        when(userService.createUser(any())).thenReturn(created);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"John","postalAddress":"1 rue de Test",
                                "email":"john.doe@test.com","password":"StrOng!Passw0rd"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@test.com"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void signup_shouldReturn400_whenEmailAlreadyUsed() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new FunctionalException("Cette adresse email est déjà utilisée."));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"John","postalAddress":"1 rue de Test",
                                "email":"john.doe@test.com","password":"StrOng!Passw0rd"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cette adresse email est déjà utilisée."));
    }
}
