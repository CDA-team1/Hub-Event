package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LegalDocumentDto;
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
 * Interface de documentation Swagger/OpenAPI du {@link LegalDocumentController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Documents légaux", description = "Gestion des documents légaux (CGU, RGPD)")
public interface LegalDocumentControllerDoc {

    @Operation(summary = "Retourne une page de documents légaux",
            description = "Pagination via les paramètres page, size et sort (ex. ?page=0&size=20&sort=id,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de documents légaux au format JSON",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class)))
    })
    PageDto<LegalDocumentDto> getAll(Pageable pageable);

    @Operation(summary = "Retourne un document légal par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document légal trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LegalDocumentDto.class))),
            @ApiResponse(responseCode = "404", description = "Aucun document légal pour cet identifiant",
                    content = @Content)
    })
    LegalDocumentDto getById(@Parameter(description = "Identifiant du document légal") Long id);

    @Operation(summary = "Crée un nouveau document légal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document légal créé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LegalDocumentDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    LegalDocumentDto create(LegalDocumentDto legalDocumentDto);

    @Operation(summary = "Met à jour un document légal existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document légal mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LegalDocumentDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun document légal pour cet identifiant",
                    content = @Content)
    })
    LegalDocumentDto update(@Parameter(description = "Identifiant du document légal") Long id, LegalDocumentDto legalDocumentDto);

    @Operation(summary = "Supprime un document légal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document légal supprimé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun document légal pour cet identifiant",
                    content = @Content)
    })
    void delete(@Parameter(description = "Identifiant du document légal") Long id);
}
