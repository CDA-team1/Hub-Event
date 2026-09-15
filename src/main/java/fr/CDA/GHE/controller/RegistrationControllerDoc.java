package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.RegistrationDto;
import fr.CDA.GHE.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * Interface de documentation Swagger/OpenAPI du {@link RegistrationController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Inscriptions", description = "Gestion des inscriptions aux évènements")
public interface RegistrationControllerDoc {

    @Operation(summary = "S'inscrire à un évènement",
            description = "Inscrit l'utilisateur connecté à l'évènement donné. "
                    + "Si l'évènement est complet, l'utilisateur est placé en liste d'attente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscription créée (REGISTERED ou WAITING_LIST)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationDto.class))),
            @ApiResponse(responseCode = "400", description = "Compte inactif, déjà inscrit, ou chevauchement d'horaire",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Évènement introuvable", content = @Content)
    })
    ResponseEntity<RegistrationDto> register(
            @Parameter(description = "Identifiant de l'évènement") Long eventId) throws FunctionalException;

    @Operation(summary = "Se désinscrire d'un évènement",
            description = "Désinscrit l'utilisateur connecté de l'évènement donné. "
                    + "Si une place se libère, la première personne en liste d'attente est promue et notifiée par email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Désinscription effectuée", content = @Content),
            @ApiResponse(responseCode = "400", description = "L'utilisateur n'est pas inscrit à cet évènement",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Évènement introuvable", content = @Content)
    })
    ResponseEntity<Void> unregister(
            @Parameter(description = "Identifiant de l'évènement") Long eventId) throws FunctionalException;
}
