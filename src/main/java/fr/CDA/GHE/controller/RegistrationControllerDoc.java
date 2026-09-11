package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.RegistrationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

/**
 * Interface de documentation Swagger/OpenAPI du {@link RegistrationController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Inscriptions", description = "Gestion des inscriptions aux évènements")
public interface RegistrationControllerDoc {

    @Operation(summary = "Retourne une page d'inscriptions",
            description = "Pagination via les paramètres page, size et sort (ex. ?page=0&size=20&sort=id,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page d'inscriptions au format JSON",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class)))
    })
    PageDto<RegistrationDto> getAll(Pageable pageable);

    @Operation(summary = "Retourne une inscription par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscription trouvée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationDto.class))),
            @ApiResponse(responseCode = "404", description = "Aucune inscription pour cet identifiant",
                    content = @Content)
    })
    RegistrationDto getById(@Parameter(description = "Identifiant de l'inscription") Long id);

    @Operation(summary = "Crée une nouvelle inscription",
            description = "Inscrit un membre à un évènement (placement en liste d'attente si l'évènement est complet)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscription créée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    RegistrationDto create(RegistrationDto registrationDto);

    @Operation(summary = "Supprime une inscription",
            description = "Désinscrit un membre de l'évènement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Inscription supprimée", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucune inscription pour cet identifiant",
                    content = @Content)
    })
    void delete(@Parameter(description = "Identifiant de l'inscription") Long id);
}
