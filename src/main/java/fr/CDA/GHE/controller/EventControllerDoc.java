package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

/**
 * Interface de documentation Swagger/OpenAPI du {@link EventController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Évènements", description = "Gestion des évènements")
public interface EventControllerDoc {

    @Operation(summary = "Retourne une page d'évènements",
            description = "Pagination via les paramètres page, size et sort (ex. ?page=0&size=20&sort=startDate,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page d'évènements au format JSON",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class)))
    })
    PageDto<EventDto> getAll(Pageable pageable);

    @Operation(summary = "Retourne un évènement par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Évènement trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventDto.class))),
            @ApiResponse(responseCode = "404", description = "Aucun évènement pour cet identifiant",
                    content = @Content)
    })
    EventDto getById(@Parameter(description = "Identifiant de l'évènement") Long id);

    @Operation(summary = "Crée un nouvel évènement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Évènement créé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    EventDto create(EventDto eventDto);

    @Operation(summary = "Met à jour un évènement existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Évènement mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun évènement pour cet identifiant",
                    content = @Content)
    })
    EventDto update(@Parameter(description = "Identifiant de l'évènement") Long id, EventDto eventDto);

    @Operation(summary = "Supprime un évènement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Évènement supprimé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun évènement pour cet identifiant",
                    content = @Content)
    })
    void delete(@Parameter(description = "Identifiant de l'évènement") Long id);
}
