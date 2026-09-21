package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.CreateCommentRequest;
import fr.CDA.GHE.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Interface de documentation Swagger/OpenAPI du {@link CommentController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 * <p>
 * Seul {@code create} est exposé (CU12 : ajouter un commentaire). La consultation se fait via
 * {@code GET /events/{id}/comments} (voir {@link EventControllerDoc}) : lister/consulter un
 * commentaire isolé, le modifier ou le supprimer ne sont demandés par aucun cas d'utilisation
 * du SFG (API-04).
 */
@Tag(name = "Commentaires", description = "Gestion des commentaires sur les évènements")
public interface CommentControllerDoc {

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
}
