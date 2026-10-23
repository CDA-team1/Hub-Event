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
 * Avec les propriétés du profil de production (SEC-12), ni la description OpenAPI ni Swagger UI ne
 * sont servies, même à un administrateur. {@link ProductionPropertiesTest} vérifie que ces mêmes
 * propriétés sont bien dans {@code application-prod.properties}.
 */
@SpringBootTest(properties = {"springdoc.api-docs.enabled=false", "springdoc.swagger-ui.enabled=false"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerDisabledInProductionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocs_shouldNotBeServed_evenToAnAdministrator() throws Exception {
        mockMvc.perform(get("/v3/api-docs").with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void swaggerUi_shouldNotBeServed_evenToAnAdministrator() throws Exception {
        mockMvc.perform(get("/swagger-ui.html").with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
