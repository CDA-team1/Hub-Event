package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AnonymizationDto;
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
 * Interface de documentation Swagger/OpenAPI du {@link AnonymizationRequestController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 * <p>
 * Note : le DTO associé s'appelle {@link AnonymizationDto} (et non AnonymizationRequestDto).
 */
@Tag(name = "Demandes d'anonymisation", description = "Gestion des demandes d'anonymisation (RGPD)")
public interface AnonymizationRequestControllerDoc {

    @Operation(summary = "Retourne une page de demandes d'anonymisation",
            description = "Pagination via les paramètres page, size et sort (ex. ?page=0&size=20&sort=id,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de demandes au format JSON",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class)))
    })
    PageDto<AnonymizationDto> getAll(Pageable pageable);

    @Operation(summary = "Retourne une demande d'anonymisation par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande trouvée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnonymizationDto.class))),
            @ApiResponse(responseCode = "404", description = "Aucune demande pour cet identifiant",
                    content = @Content)
    })
    AnonymizationDto getById(@Parameter(description = "Identifiant de la demande") Long id);

    @Operation(summary = "Crée une nouvelle demande d'anonymisation",
            description = "Un membre demande l'anonymisation de son compte")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Demande créée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnonymizationDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    AnonymizationDto create(AnonymizationDto anonymizationDto);

    @Operation(summary = "Met à jour une demande d'anonymisation",
            description = "Traitement de la demande par un administrateur (validation)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande mise à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnonymizationDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucune demande pour cet identifiant",
                    content = @Content)
    })
    AnonymizationDto update(@Parameter(description = "Identifiant de la demande") Long id, AnonymizationDto anonymizationDto);
}
