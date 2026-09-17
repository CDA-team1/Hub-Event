package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.CreateCommentRequest;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

/**
 * Interface de documentation Swagger/OpenAPI du {@link CommentController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Commentaires", description = "Gestion des commentaires sur les évènements")
public interface CommentControllerDoc {

    @Operation(summary = "Retourne une page de commentaires",
            description = "Pagination via les paramètres page, size et sort (ex. ?page=0&size=20&sort=id,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de commentaires au format JSON",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class)))
    })
    PageDto<CommentDto> getAll(Pageable pageable);

    @Operation(summary = "Retourne un commentaire par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commentaire trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Aucun commentaire pour cet identifiant",
                    content = @Content)
    })
    CommentDto getById(@Parameter(description = "Identifiant du commentaire") Long id);

    @Operation(summary = "Publie un nouveau commentaire sur un événement",
            description = "Réservé aux utilisateurs connectés disposant d'un compte actif (CU12).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Commentaire créé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Contenu vide, ou événement introuvable",
                    content = @Content)
    })
    CommentDto create(CreateCommentRequest request) throws FunctionalException;

    @Operation(summary = "Met à jour un commentaire existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commentaire mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun commentaire pour cet identifiant",
                    content = @Content)
    })
    CommentDto update(@Parameter(description = "Identifiant du commentaire") Long id, CommentDto commentDto);

    @Operation(summary = "Supprime un commentaire")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Commentaire supprimé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun commentaire pour cet identifiant",
                    content = @Content)
    })
    void delete(@Parameter(description = "Identifiant du commentaire") Long id);
}
