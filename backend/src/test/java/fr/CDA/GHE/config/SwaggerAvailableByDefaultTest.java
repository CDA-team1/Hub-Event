package fr.CDA.GHE.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Témoin de {@link SwaggerDisabledInProductionTest} : sans les propriétés du profil de production, la
 * description OpenAPI est servie. Sans ce témoin, un 404 pourrait venir d'une autre cause.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerAvailableByDefaultTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocs_shouldBeAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs").with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
