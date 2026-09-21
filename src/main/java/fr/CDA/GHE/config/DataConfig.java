package fr.CDA.GHE.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Point d'entrée pour amorcer des données au démarrage de l'application, si besoin.
 * <p>
 * {@link CommandLineRunner} : Spring exécute {@link #run(String...)} une fois le contexte
 * entièrement démarré — donc après la création des tables.
 * <p>
 * Actuellement inactif : les comptes de démo sont désormais chargés via
 * {@code src/main/resources/data.sql} (CPT-07), pas via du code Java. Cette classe est
 * conservée (avec le flag {@code application.init}) pour un futur amorçage nécessitant de
 * la logique métier (ex. appeler un service, un {@code PasswordEncoder}...) que
 * {@code data.sql} ne peut pas faire tout seul.
 */
@Component
public class DataConfig implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataConfig.class);

  /** Active ou non l'amorçage (voir application.properties). */
  @Value("${application.init}")
  private boolean initData;

  @Override
  public void run(String... args) {
    if (!initData) {
      log.info("Amorçage des données désactivé (application.init=false).");
      return;
    }
  }
}
