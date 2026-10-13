package fr.CDA.GHE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// EnableAsync : active @Async, utilisé par EmailService pour envoyer les notifications en tâche de fond.
@SpringBootApplication
@EnableAsync
public class GheApplication {

	public static void main(String[] args) {
		SpringApplication.run(GheApplication.class, args);
	}

}
