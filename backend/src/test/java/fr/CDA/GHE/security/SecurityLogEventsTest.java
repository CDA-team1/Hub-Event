package fr.CDA.GHE.security;

import fr.CDA.GHE.LogCapture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Événements de sécurité vus de l'extérieur (SEC-14) : requêtes HTTP réelles sur le contexte
 * complet, journal {@code SECURITY} écouté.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityLogEventsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void failedLogin_shouldLogMaskedEmailAndAddress_butNeverThePassword() throws Exception {
        try (LogCapture security = new LogCapture("SECURITY")) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"ghost@test.com\",\"password\":\"SuperSecret1!\"}"))
                    .andExpect(status().isUnauthorized());

            assertThat(security.all())
                    .contains("CONNEXION refusée", "g***@test.com", "ip=127.0.0.1")
                    .doesNotContain("ghost@test.com")
                    .doesNotContain("SuperSecret1!");
        }
    }

    @Test
    void forbiddenAccess_shouldLogMethodAndPath_butNeverTheQueryString() throws Exception {
        try (LogCapture security = new LogCapture("SECURITY")) {
            mockMvc.perform(get("/admin/users")
                            .param("token", "JETON-SECRET-DANS-LURL")
                            .with(user("member@test.com").roles("MEMBER")))
                    .andExpect(status().isForbidden());

            assertThat(security.all())
                    .contains("ACCÈS REFUSÉ", "GET", "/admin/users")
                    .doesNotContain("JETON-SECRET-DANS-LURL")
                    .doesNotContain("member@test.com");
        }
    }
}
