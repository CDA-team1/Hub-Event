package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AnonymizationDto;
import fr.CDA.GHE.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Interface de documentation Swagger/OpenAPI du {@link AnonymizationRequestController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 * <p>
 * Note : le DTO associé s'appelle {@link AnonymizationDto} (et non AnonymizationRequestDto).
 * Consultation/validation admin : voir {@link AdminAnonymizationControllerDoc}.
 */
@Tag(name = "Demandes d'anonymisation", description = "Auto-inscription à une demande d'anonymisation (RGPD)")
public interface AnonymizationRequestControllerDoc {

    @Operation(summary = "Crée une nouvelle demande d'anonymisation",
            description = "Enregistre une demande d'anonymisation pour l'utilisateur connecté (identifié via "
                    + "le JWT). Aucune donnée à transmettre : ni doublon, ni demande pour un autre compte.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Demande créée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnonymizationDto.class))),
            @ApiResponse(responseCode = "400", description = "Compte inactif ou demande déjà existante",
                    content = @Content)
    })
    AnonymizationDto create() throws FunctionalException;
}
