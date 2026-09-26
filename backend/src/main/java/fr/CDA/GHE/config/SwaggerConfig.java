package fr.CDA.GHE.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI/Swagger de l'API.
 * <p>
 * Définit le titre, la version et la description affichés dans l'interface Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hub évènementiel — API de gestion")
                        .version("1.0")
                        .description("""
                                API REST de gestion d'un hub évènementiel pour une fédération régionale de clubs.

                                Elle couvre :
                                - la gestion des **clubs** de la fédération ;
                                - le cycle de vie des **évènements** (brouillon, publié, annulé, terminé) ;
                                - les **inscriptions** aux évènements, avec mise en liste d'attente quand la capacité est atteinte ;
                                - les **commentaires** laissés sur les évènements ;
                                - les **comptes utilisateurs** et leurs rôles (membre, organisateur, administrateur) ;
                                - les **documents légaux** (CGU, RGPD) et les **demandes d'anonymisation** des comptes (RGPD).

                                Les routes sont sécurisées par **JWT** et restreintes selon le rôle de l'utilisateur.

                                **Réalisée par l'équipe projet** dans le cadre du titre CDA :
                                Damien Castello · Anthony · Garance
                                """));
    }
}
