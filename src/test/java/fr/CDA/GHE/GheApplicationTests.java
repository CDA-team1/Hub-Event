package fr.CDA.GHE;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests du contexte Spring Boot de l'application.
 * Vérifie que l'application démarre correctement avec le profil de test.
 */
@ActiveProfiles("test")
@SpringBootTest
class GheApplicationTests {

  /**
   * Vérifie que le contexte Spring se charge sans erreur.
   */
  @Test
  void contextLoads() {
  }

}
