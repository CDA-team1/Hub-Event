package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.CreateEventRequest;
import fr.CDA.GHE.dto.UpdateEventRequest;
import fr.CDA.GHE.exception.FunctionalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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

  @Operation(summary = "Supprime un évènement")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Évènement supprimé", content = @Content),
      @ApiResponse(responseCode = "404", description = "Aucun évènement pour cet identifiant",
          content = @Content)
  })
  void delete(@Parameter(description = "Identifiant de l'évènement") Long id);

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
}
