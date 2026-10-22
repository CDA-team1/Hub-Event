package fr.CDA.GHE.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garde-fou (SEC-12) : relit les fichiers de configuration livrés et vérifie les réglages qui
 * protègent la production. Une modification involontaire (ex. copier le profil de développement
 * dans le profil de production) fait échouer ce test.
 */
class ProductionPropertiesTest {

    private static Properties load(String file) throws IOException {
        return PropertiesLoaderUtils.loadAllProperties(file);
    }

    @Test
    void prodProfile_shouldNeverTouchTheSchemaNorLoadDemoData() throws IOException {
        Properties prod = load("application-prod.properties");

        assertThat(prod.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(prod.getProperty("spring.sql.init.mode")).isEqualTo("never");
        assertThat(prod.getProperty("application.init")).isEqualTo("false");
    }

    @Test
    void prodProfile_shouldNotLogSql() throws IOException {
        assertThat(load("application-prod.properties").getProperty("spring.jpa.show-sql")).isEqualTo("false");
    }

    @Test
    void prodProfile_shouldDisableApiDocumentation() throws IOException {
        Properties prod = load("application-prod.properties");

        assertThat(prod.getProperty("springdoc.api-docs.enabled")).isEqualTo("false");
        assertThat(prod.getProperty("springdoc.swagger-ui.enabled")).isEqualTo("false");
    }

    @Test
    void prodProfile_shouldNotHardcodeCredentials() throws IOException {
        Properties prod = load("application-prod.properties");

        assertThat(prod.getProperty("spring.datasource.url")).startsWith("${");
        assertThat(prod.getProperty("spring.datasource.username")).startsWith("${");
        assertThat(prod.getProperty("spring.datasource.password")).startsWith("${");
    }

    @Test
    void commonConfiguration_shouldNeverExposeErrorDetails() throws IOException {
        Properties common = load("application.properties");

        assertThat(common.getProperty("server.error.include-message")).isEqualTo("never");
        assertThat(common.getProperty("server.error.include-stacktrace")).isEqualTo("never");
        assertThat(common.getProperty("server.error.include-binding-errors")).isEqualTo("never");
        assertThat(common.getProperty("server.error.include-exception")).isEqualTo("false");
    }
}
