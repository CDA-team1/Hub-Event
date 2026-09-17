package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.dto.CreateEventRequest;
import fr.CDA.GHE.dto.ImageContentDto;
import fr.CDA.GHE.dto.ImageDto;
import fr.CDA.GHE.dto.UpdateEventRequest;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.CommentService;
import fr.CDA.GHE.service.EventService;
import fr.CDA.GHE.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * SEC-02 — Autorisations des routes évènements (voir {@link ClubController} pour les règles générales).
 * <p>
 * Les GET sont <strong>publics</strong> (liste des publiés, détail, recherche). La recherche
 * ({@code GET /events/search}) est un endpoint distinct, non présent dans {@link EventControllerDoc} :
 * à laisser public quand il sera créé.
 * <p>
 * {@code update}/{@code delete} : en plus du rôle, l'organisateur ne peut agir que sur les évènements
 * de SON club → contrôle de <strong>propriété</strong> à faire dans le service (403 sinon).
 */
@RestController
@RequestMapping("/events")
public class EventController implements EventControllerDoc {

  /**
   * Service de gestion des événements utilisé par ce contrôleur.
   */
  private final EventService eventService;

  private final ImageService imageService;

  private final CommentService commentService;

  /**
   * Initialise le contrôleur avec les services de gestion des événements, des images et des
   * commentaires.
   *
   * @param eventService   service de gestion des événements
   * @param imageService   service de gestion de la galerie photos des événements
   * @param commentService service de gestion des commentaires des événements
   */
  public EventController(EventService eventService, ImageService imageService, CommentService commentService) {
    this.eventService = eventService;
    this.imageService = imageService;
    this.commentService = commentService;
  }

  // Public : consultation des évènements publiés et terminés
  @Override
  @GetMapping
  public EventListDto getAll() {
    return eventService.getPublicEvents();
  }

  /**
   * Retourne le détail d'un événement accessible.
   *
   * @param id identifiant de l'événement
   * @return le détail de l'événement
   */
  @Override
  @GetMapping("/{id}")
  public EventDetailResponse getById(@PathVariable Long id) {
    return eventService.getEventDetail(id);
  }

  @Override
  @Secured("ROLE_ORGANIZER")
  @PostMapping("/{id}/publish")
  public EventDto publish(@PathVariable Long id)
      throws FunctionalException {

    return eventService.publishEvent(id);
  }

  @Override
  @Secured("ROLE_ORGANIZER")
  @PostMapping("/{id}/status")
  public EventDto finish(@PathVariable Long id)
      throws FunctionalException {

    return eventService.finishEvent(id);
  }

  @Override
  @Secured("ROLE_ORGANIZER")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EventDto create(@RequestBody CreateEventRequest request)
      throws FunctionalException {

    return eventService.createEvent(request);
  }

  @Override
  @Secured("ROLE_ORGANIZER")
  @PutMapping("/{id}")
  public EventDto update(
      @PathVariable Long id,
      @RequestBody UpdateEventRequest request
  ) throws FunctionalException {

    return eventService.updateEvent(id, request);
  }

  @Override
  @Secured({"ROLE_ORGANIZER", "ROLE_ADMIN"})
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    // TODO SEC-02 : contrôle de propriété (organisateur du club) → 403 si non propriétaire
    throw new UnsupportedOperationException("TODO logique métier (tâche feature Évènement)");
  }

  // Public : consultation d'une image de la galerie
  @Override
  @GetMapping("/{eventId}/images/{imageId}")
  public ResponseEntity<byte[]> getImage(@PathVariable Long eventId, @PathVariable Long imageId) {
    ImageContentDto image = imageService.getImageContent(eventId, imageId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(image.contentType()))
        .body(image.content());
  }

  @Override
  @Secured("ROLE_ORGANIZER")
  @PostMapping(value = "/{eventId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public List<ImageDto> addImages(@PathVariable Long eventId, @RequestParam("files") List<MultipartFile> files)
      throws FunctionalException {
    return imageService.addImages(eventId, files);
  }

  @Override
  @Secured("ROLE_ORGANIZER")
  @DeleteMapping("/{eventId}/images/{imageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeImage(@PathVariable Long eventId, @PathVariable Long imageId) {
    imageService.removeImage(eventId, imageId);
  }

  // Public : consultation des commentaires d'un événement
  @Override
  @GetMapping("/{id}/comments")
  public List<CommentDto> getComments(@PathVariable Long id) {
    return commentService.extractByEvent(id);
  }
}
