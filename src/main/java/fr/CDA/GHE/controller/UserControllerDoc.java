package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

/**
 * Interface de documentation Swagger/OpenAPI du {@link UserController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Utilisateurs", description = "Gestion des comptes utilisateurs")
public interface UserControllerDoc {

    @Operation(summary = "Retourne une page d'utilisateurs",
            description = "Pagination via les paramètres page, size et sort (ex. ?page=0&size=20&sort=email,asc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page d'utilisateurs au format JSON",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class)))
    })
    PageDto<UserDto> getAll(Pageable pageable);

    @Operation(summary = "Retourne un utilisateur par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet identifiant",
                    content = @Content)
    })
    UserDto getById(@Parameter(description = "Identifiant de l'utilisateur") Long id);

    @Operation(summary = "Crée un compte (membre affilié, organisateur ou administrateur)",
            description = "Réservé à l'administrateur (CdC §Création d'un compte). Distinct de l'inscription "
                    + "self-service (POST /auth/signup) : mot de passe temporaire, club/rôle choisis par l'admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compte créé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    UserDto create(UserDto userDto);

    @Operation(summary = "Met à jour un utilisateur existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet identifiant",
                    content = @Content)
    })
    UserDto update(@Parameter(description = "Identifiant de l'utilisateur") Long id, UserDto userDto);

    @Operation(summary = "Supprime un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet identifiant",
                    content = @Content)
    })
    void delete(@Parameter(description = "Identifiant de l'utilisateur") Long id);
}
