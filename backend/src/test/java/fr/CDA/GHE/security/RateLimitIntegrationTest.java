package fr.CDA.GHE.security;

import fr.CDA.GHE.controller.AuthController;
import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.service.AuthService;
import fr.CDA.GHE.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie le câblage réel de la limitation de débit (SEC-09) : filtre branché dans la chaîne de
 * sécurité de {@code SecurityConfig}, sur la vraie route {@code POST /auth/login}. La limitation,
 * coupée pour les autres tests, est réactivée ici.
 */
@WebMvcTest(AuthController.class)
@TestPropertySource(properties = "app.rate-limit.enabled=true")
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private void login() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("jwt", Role.MEMBER));
    }

    @Test
    void login_shouldAnswer429_afterTenAttemptsFromTheSameAddress() throws Exception {
        login();

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"john.doe@test.com\",\"password\":\"secret\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john.doe@test.com\",\"password\":\"secret\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
