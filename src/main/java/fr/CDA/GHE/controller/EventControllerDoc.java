package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.CreateEventRequest;
import fr.CDA.GHE.dto.ImageDto;
import fr.CDA.GHE.dto.UpdateEventRequest;
import fr.CDA.GHE.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * Interface de documentation Swagger/OpenAPI du {@link EventController}.
 * Voir {@link ClubControllerDoc} pour le détail du patron (séparation doc / mapping).
 */
@Tag(name = "Évènements", description = "Gestion des évènements")
public interface EventControllerDoc {

  @Operation(
      summary = "Retourne les événements disponibles à la consultation publique",
      description = "Retourne les événements PUBLISHED regroupés par catégorie "
          + "et les événements FINISHED dans la rubrique des événements passés.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Listes d'événements retournées avec succès",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EventListDto.class)))
  })
  EventListDto getAll();

  @Operation(
      summary = "Retourne le détail d'un événement par son identifiant",
      description = "Retourne le détail d'un événement accessible à l'utilisateur courant.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Détail de l'événement retourné avec succès",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EventDetailResponse.class)
          )),
      @ApiResponse(
          responseCode = "404",
          description = "Événement introuvable ou non accessible",
          content = @Content)
  })
  EventDetailResponse getById(
      @Parameter(description = "Identifiant de l'événement") Long id
  );

  /**
   * Télécharge la fiche détaillée d'un événement au format PDF.
   *
   * @param id identifiant de l'événement
   * @return réponse HTTP contenant le fichier PDF généré
   */
  @Operation(
      summary = "Télécharge la fiche détaillée d'un événement au format PDF",
      description = "Applique les mêmes règles d'accès que la consultation "
          + "du détail de l'événement."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Fiche PDF générée avec succès",
          content = @Content(
              mediaType = "application/pdf",
              schema = @Schema(type = "string", format = "binary")
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Événement introuvable ou non accessible",
          content = @Content
      )
  })
  ResponseEntity<byte[]> getPdf(
      @Parameter(description = "Identifiant de l'événement") Long id
  );

  @Operation(summary = "Crée un nouvel évènement")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201",
          description = "Évènement créé",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EventDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Données invalides",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Authentification requise",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Accès refusé : rôle ORGANIZER requis",
          content = @Content
      )
  })
  EventDto create(CreateEventRequest request) throws FunctionalException;

  @Operation(summary = "Publie un événement")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Événement publié",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EventDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "L'événement ne peut pas être publié dans son état actuel",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Authentification requise",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Utilisateur non autorisé à publier cet événement",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Événement introuvable",
          content = @Content
      )
  })
  EventDto publish(
      @Parameter(description = "Identifiant de l'événement") Long id
  ) throws FunctionalException;


  @Operation(summary = "Met à jour un évènement existant")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Évènement mis à jour",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EventDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Données invalides",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Aucun évènement pour cet identifiant",
          content = @Content
      )
  })
  EventDto update(
      @Parameter(description = "Identifiant de l'évènement") Long id,
      UpdateEventRequest request
  ) throws FunctionalException;

  @Operation(summary = "Termine un événement")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Événement terminé",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EventDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Changement de statut non autorisé",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Authentification requise",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Utilisateur non autorisé à modifier le statut de cet événement",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Événement introuvable",
          content = @Content
      )
  })
  EventDto finish(
      @Parameter(description = "Identifiant de l'événement") Long id
  ) throws FunctionalException;

  @Operation(summary = "Supprime définitivement un évènement")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204",
          description = "Évènement supprimé",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "400",
          description = "L'évènement ne peut pas être supprimé",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Utilisateur non authentifié",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Utilisateur non autorisé à supprimer cet évènement",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Aucun évènement pour cet identifiant",
          content = @Content
      )
  })
  void delete(
      @Parameter(description = "Identifiant de l'évènement") Long id
  ) throws FunctionalException;

  @Operation(summary = "Annule un événement")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Événement annulé",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EventDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Événement non annulable dans son état actuel",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Authentification requise",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Utilisateur non autorisé à annuler cet événement",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Événement introuvable",
          content = @Content
      )
  })
  EventDto cancel(
      @Parameter(description = "Identifiant de l'événement") Long id
  ) throws FunctionalException;

  @Operation(
      summary = "Retourne le contenu d'une image de la galerie",
      description = "Accessible sans authentification (CU23)."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Image trouvée",
          content = @Content(mediaType = "image/*")
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Événement ou image introuvable",
          content = @Content
      )
  })
  ResponseEntity<byte[]> getImage(
      @Parameter(description = "Identifiant de l'événement") Long eventId,
      @Parameter(description = "Identifiant de l'image") Long imageId
  );

  @Operation(
      summary = "Ajoute une ou plusieurs images à la galerie d'un événement",
      description = "Réservé à l'organisateur propriétaire de l'événement (CU23, règles n°1 à 3)."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201",
          description = "Images ajoutées",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ImageDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Aucun fichier fourni, ou fichier non image",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Utilisateur non autorisé à modifier cette galerie",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Événement introuvable",
          content = @Content
      )
  })
  List<ImageDto> addImages(
      @Parameter(description = "Identifiant de l'événement") Long eventId,
      List<MultipartFile> files
  ) throws FunctionalException;

  @Operation(
      summary = "Retire une image de la galerie d'un événement",
      description = "Réservé à l'organisateur propriétaire de l'événement (CU23, règle n°4)."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204",
          description = "Image retirée",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Utilisateur non autorisé à modifier cette galerie",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Événement ou image introuvable",
          content = @Content
      )
  })
  void removeImage(
      @Parameter(description = "Identifiant de l'événement") Long eventId,
      @Parameter(description = "Identifiant de l'image") Long imageId
  );

  @Operation(
      summary = "Retourne les commentaires d'un événement",
      description = "Accessible sans authentification, trié du plus ancien au plus récent (CU12)."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Commentaires de l'événement",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CommentDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Événement introuvable",
          content = @Content
      )
  })
  List<CommentDto> getComments(@Parameter(description = "Identifiant de l'événement") Long id);
}
