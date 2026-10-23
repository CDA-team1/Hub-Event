package fr.CDA.GHE.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Durcissement HTTP (SEC-12) sur le contexte complet : codes d'erreur corrects et sans détail
 * technique, et en-têtes de protection posés par Spring Security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HttpHardeningTest {

    @Autowired
    private MockMvc mockMvc;

    private void assertNoTechnicalDetail(MvcResult result) throws Exception {
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("Exception", "org.springframework", "at fr.CDA", ".java");
    }

    @Test
    void unknownUrl_shouldAnswer404WithoutTechnicalDetail() throws Exception {
        MvcResult result = mockMvc.perform(get("/route-inconnue").with(user("a@test.com").roles("MEMBER")))
                .andExpect(status().isNotFound())
                .andReturn();

        assertNoTechnicalDetail(result);
    }

    @Test
    void wrongMethod_shouldAnswer405WithoutTechnicalDetail() throws Exception {
        MvcResult result = mockMvc.perform(delete("/auth/login").with(user("a@test.com").roles("MEMBER")))
                .andExpect(status().isMethodNotAllowed())
                .andReturn();

        assertNoTechnicalDetail(result);
    }

    @Test
    void unsupportedContentType_shouldAnswer415WithoutTechnicalDetail() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("email=a&password=b"))
                .andExpect(status().isUnsupportedMediaType())
                .andReturn();

        assertNoTechnicalDetail(result);
    }

    @Test
    void responses_shouldCarrySecurityHeaders() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test
    void httpsResponses_shouldAskBrowsersToStayOnHttps() throws Exception {
        mockMvc.perform(get("/events").secure(true))
                .andExpect(header().string("Strict-Transport-Security",
                        org.hamcrest.Matchers.containsString("max-age=")));
    }

    @Test
    void responses_shouldNotRevealTheServerSoftware() throws Exception {
        MvcResult result = mockMvc.perform(get("/events")).andReturn();

        assertThat(result.getResponse().getHeader("Server")).isNull();
        assertThat(result.getResponse().getHeader("X-Powered-By")).isNull();
    }
}
