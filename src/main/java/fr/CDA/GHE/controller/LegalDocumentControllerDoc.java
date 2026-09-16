package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.LegalDocumentDto;
import fr.CDA.GHE.entity.enums.DocumentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * Interface de documentation Swagger/OpenAPI du {@link LegalDocumentController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Documents légaux", description = "Consultation publique des CGU et de la politique RGPD (CU7, CU8)")
public interface LegalDocumentControllerDoc {

    @Operation(summary = "Retourne le contenu d'un document légal",
            description = "Accessible sans authentification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LegalDocumentDto.class))),
            @ApiResponse(responseCode = "404", description = "Aucun document enregistré pour ce type",
                    content = @Content)
    })
    LegalDocumentDto getByType(@Parameter(description = "Type de document (RGPD ou CGU)") DocumentType type);

    @Operation(summary = "Retourne un document légal au format PDF",
            description = "Accessible sans authentification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF généré",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Aucun document enregistré pour ce type",
                    content = @Content)
    })
    ResponseEntity<byte[]> getByTypePdf(@Parameter(description = "Type de document (RGPD ou CGU)") DocumentType type);
}
