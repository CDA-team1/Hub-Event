package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AdminUserRequest;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.UserDto;
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
 * Interface de documentation Swagger/OpenAPI du {@link UserController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Utilisateurs (admin)", description = "Gestion des comptes utilisateurs par un administrateur (CU25)")
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
            description = "Réservé à l'administrateur (CU25, SFG §2.28). Distinct de l'inscription "
                    + "self-service (POST /auth/signup) : rôle et clubs choisis par l'admin, mot de passe "
                    + "temporaire généré par le système et envoyé par email. L'email doit être unique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compte créé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé",
                    content = @Content)
    })
    UserDto create(AdminUserRequest request) throws FunctionalException;

    @Operation(summary = "Met à jour un utilisateur existant",
            description = "Réservé à l'administrateur (CU25, SFG §2.28). L'email doit rester unique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet identifiant",
                    content = @Content)
    })
    UserDto update(@Parameter(description = "Identifiant de l'utilisateur") Long id, AdminUserRequest request)
            throws FunctionalException;

    @Operation(summary = "Supprime un utilisateur",
            description = "Réservé à l'administrateur (CU25, SFG §2.28).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet identifiant",
                    content = @Content)
    })
    void delete(@Parameter(description = "Identifiant de l'utilisateur") Long id);
}
