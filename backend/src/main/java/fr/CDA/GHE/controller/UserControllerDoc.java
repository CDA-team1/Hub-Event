package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AdminUserRequest;
import fr.CDA.GHE.dto.ClubAffiliationRequest;
import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.SuspendUserRequest;
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

import java.util.List;

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

    @Operation(summary = "Gère les affiliations club d'un membre ou d'un organisateur",
            description = "Remplace l'ensemble des clubs auxquels l'utilisateur est rattaché par la liste "
                    + "transmise (CU26, CU27). Une liste vide est valide. Si un organisateur n'a plus aucun "
                    + "club, il est rétrogradé en membre non affilié. Ne s'applique pas aux administrateurs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clubs affiliés après mise à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClubDto.class))),
            @ApiResponse(responseCode = "400", description = "Utilisateur administrateur, ou club introuvable",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet identifiant",
                    content = @Content)
    })
    List<ClubDto> updateAffiliations(@Parameter(description = "Identifiant de l'utilisateur") Long id,
                                      ClubAffiliationRequest request) throws FunctionalException;

    @Operation(summary = "Suspend un compte utilisateur",
            description = "Réservé à l'administrateur (SUSP-01). Une suspension sans date de fin est "
                    + "définitive, avec une date de fin elle est temporaire. Le motif est obligatoire. "
                    + "L'utilisateur est notifié par email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte suspendu", content = @Content),
            @ApiResponse(responseCode = "400", description = "Motif manquant", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet identifiant",
                    content = @Content)
    })
    void suspend(@Parameter(description = "Identifiant de l'utilisateur") Long id, SuspendUserRequest request)
            throws FunctionalException;
}
