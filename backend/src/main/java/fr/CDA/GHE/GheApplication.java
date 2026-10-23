package fr.CDA.GHE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Point d'entrée de l'application Hub-Event.
 *
 * <p>Active également l'exécution asynchrone utilisée notamment pour l'envoi
 * des notifications par e-mail.</p>
 */
@SpringBootApplication
@EnableAsync
public class GheApplication {

	public static void main(String[] args) {
		SpringApplication.run(GheApplication.class, args);
	}

}
