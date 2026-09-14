package fr.CDA.GHE.config;

import fr.CDA.GHE.service.JpaUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Amorce des comptes de démo au démarrage de l'application.
 * <p>
 * {@link CommandLineRunner} : Spring exécute {@link #run(String...)} une fois le contexte
 * entièrement démarré — donc après la création des tables. Séparer le déclencheur (cette
 * classe) de la logique ({@code JpaUserDetailsService.initData()}) permet à
 * {@code @Transactional} de s'appliquer réellement (l'appel passe par le proxy Spring,
 * ce qui ne serait pas le cas d'un appel interne à la même classe).
 */
@Component
public class DataConfig implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataConfig.class);

  /** Active ou non l'amorçage (voir application.properties). */
  @Value("${application.init}")
  private boolean initData;

  private final JpaUserDetailsService userDetailsService;

  public DataConfig(JpaUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void run(String... args) {
    if (!initData) {
      log.info("Amorçage des données désactivé (application.init=false).");
      return;
    }

    userDetailsService.initData();
  }
}
