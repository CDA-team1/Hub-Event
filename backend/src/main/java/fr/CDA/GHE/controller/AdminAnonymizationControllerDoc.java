package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AnonymizationDto;
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
 * Interface de documentation Swagger/OpenAPI du {@link AdminAnonymizationController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Demandes d'anonymisation (admin)", description = "Consultation et validation des demandes d'anonymisation par un administrateur (CU28, CU29)")
public interface AdminAnonymizationControllerDoc {

    @Operation(summary = "Retourne une page de demandes d'anonymisation",
            description = "Inclut les informations de l'utilisateur concerné pour chaque demande. "
                    + "Pagination via les paramètres page, size et sort (ex. ?page=0&size=20&sort=requestDate,asc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de demandes au format JSON",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageDto.class)))
    })
    PageDto<AnonymizationDto> getAll(Pageable pageable);

    @Operation(summary = "Valide une demande d'anonymisation",
            description = "Anonymise le compte de l'utilisateur concerné : données personnelles remplacées par "
                    + "des valeurs aléatoires, statut ANONYMIZED. Si l'utilisateur est organisateur, ses "
                    + "évènements publiés à venir sont annulés, sans transfert (CU29). Aucune donnée à transmettre.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande validée, compte anonymisé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnonymizationDto.class))),
            @ApiResponse(responseCode = "400", description = "Demande déjà traitée", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aucune demande pour cet identifiant",
                    content = @Content)
    })
    AnonymizationDto validate(@Parameter(description = "Identifiant de la demande") Long id) throws FunctionalException;
}
