package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.UpdateUserRequest;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Interface de documentation Swagger/OpenAPI du {@link UserProfileController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Mon compte", description = "Consultation et modification de son propre compte (CU13)")
public interface UserProfileControllerDoc {

    @Operation(summary = "Modifie le compte de l'utilisateur connecté",
            description = "Email revérifié unique uniquement s'il change. Mot de passe laissé vide = non "
                    + "modifié ; renseigné = doit être fort, mais n'est PAS appliqué immédiatement : un email "
                    + "de confirmation est envoyé, et l'ancien mot de passe reste actif tant que le lien "
                    + "n'a pas été cliqué (GET /auth/confirm-password-change). Les clubs affiliés ne sont "
                    + "pas modifiables ici.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé",
                    content = @Content)
    })
    UserDto update(UpdateUserRequest request) throws FunctionalException;
}
