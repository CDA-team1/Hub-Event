package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.ClubDto;
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

import java.util.List;

/**
 * Interface de documentation Swagger/OpenAPI du {@link ClubController}.
 * <p>
 * Objectif : sortir du controller toutes les annotations {@code @Operation} /
 * {@code @ApiResponses} (très verbeuses) pour garder le controller lisible. Le
 * controller se contente d'{@code implements ClubControllerDoc} + des annotations
 * de mapping Spring MVC ({@code @GetMapping}, etc.) ; springdoc va lire la
 * documentation sur les méthodes héritées de cette interface.
 * <p>
 * Les signatures ici doivent rester identiques à celles du controller, sinon
 * l'{@code @Override} ne sera pas reconnu et la doc ne sera pas rattachée.
 */
@Tag(name = "Clubs", description = "Gestion des clubs de la fédération")
public interface ClubControllerDoc {

    /**
     * Retourne une page de clubs.
     *
     * @param pageable paramètres de pagination (page, size, sort)
     * @return une page de {@link ClubDto}
     */
    @Operation(summary = "Retourne une page de clubs actifs (non désaffiliés), réservé à l'administrateur",
            description = "Pagination via les paramètres page, size et sort (ex. ?page=0&size=20&sort=name,asc). "
                    + "Les clubs désaffiliés (validityEndDate renseignée) ne sont pas inclus.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de clubs au format JSON",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class))),
            @ApiResponse(responseCode = "403", description = "Rôle administrateur requis",
                    content = @Content)
    })
    PageDto<ClubDto> getAll(Pageable pageable);

    /**
     * Retourne les clubs actifs de l'organisateur connecté.
     *
     * @return les {@link ClubDto} des clubs actifs de l'organisateur
     */
    @Operation(summary = "Retourne les clubs actifs de l'organisateur connecté, réservé à l'organisateur",
            description = "Utilisé pour choisir le club organisateur à la création d'un événement (EVT-06) : "
                    + "les clubs désaffiliés (validityEndDate renseignée) ne sont pas inclus.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clubs actifs de l'organisateur",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClubDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rôle organisateur requis", content = @Content)
    })
    List<ClubDto> getMine();

    /**
     * Retourne un club par son identifiant.
     *
     * @param id identifiant du club
     * @return le {@link ClubDto} correspondant
     */
    @Operation(summary = "Retourne un club par son identifiant, réservé à l'administrateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClubDto.class))),
            @ApiResponse(responseCode = "403", description = "Rôle administrateur requis",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun club pour cet identifiant",
                    content = @Content)
    })
    ClubDto getById(@Parameter(description = "Identifiant du club") Long id);

    /**
     * Crée un nouveau club.
     *
     * @param clubDto données du club à créer
     * @return le club créé
     */
    @Operation(summary = "Crée un nouveau club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Club créé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClubDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content)
    })
    ClubDto create(ClubDto clubDto) throws FunctionalException;

    /**
     * Met à jour un club existant.
     *
     * @param id      identifiant du club à modifier
     * @param clubDto nouvelles données du club
     * @return le club mis à jour
     */
    @Operation(summary = "Met à jour un club existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club mis à jour",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClubDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun club pour cet identifiant",
                    content = @Content)
    })
    ClubDto update(@Parameter(description = "Identifiant du club") Long id, ClubDto clubDto) throws FunctionalException;

    /**
     * Supprime un club.
     *
     * @param id identifiant du club à supprimer
     */
    @Operation(summary = "Supprime un club",
            description = "Met fin à l'affiliation du club (le club n'est pas retiré de la base, "
                    + "afin de préserver l'historique de ses événements) : supprime ses "
                    + "affiliations, rétrograde les organisateurs sans autre club, annule ses "
                    + "événements futurs publiés (CU24).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Club supprimé", content = @Content),
            @ApiResponse(responseCode = "400", description = "Le club est déjà supprimé",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucun club pour cet identifiant",
                    content = @Content)
    })
    void delete(@Parameter(description = "Identifiant du club") Long id) throws FunctionalException;
}
