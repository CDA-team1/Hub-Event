package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.dto.LegalDocumentRequest;
import fr.CDA.GHE.entity.enums.DocumentType;
import fr.CDA.GHE.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Interface de documentation Swagger/OpenAPI du {@link LegalDocumentController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Documents légaux (admin)", description = "Saisie et mise à jour des CGU et de la politique RGPD (CU31, CU32)")
public interface LegalDocumentControllerDoc {

    @Operation(summary = "Enregistre ou met à jour un document légal",
            description = "Crée le document s'il n'existe pas encore pour ce type, sinon met à jour son contenu "
                    + "et sa date de mise à jour. Réservé à l'administrateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document enregistré",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LegalDocumentDto.class))),
            @ApiResponse(responseCode = "400", description = "Contenu invalide", content = @Content)
    })
    LegalDocumentDto upsert(@Parameter(description = "Type de document (RGPD ou CGU)") DocumentType type,
                             LegalDocumentRequest request) throws FunctionalException;
}
