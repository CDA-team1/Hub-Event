package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.dto.EventSearchCriteria;
import fr.CDA.GHE.dto.CreateEventRequest;
import fr.CDA.GHE.dto.ImageDto;
import fr.CDA.GHE.dto.UpdateEventRequest;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.CommentService;
import fr.CDA.GHE.service.EventService;
import fr.CDA.GHE.service.ImageService;
import fr.CDA.GHE.service.EventPdfService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * SEC-02 — Autorisations des routes évènements (voir {@link ClubController} pour les règles générales).
 * <p>
 * Les GET sont <strong>publics</strong> (liste des publiés, détail, recherche). La recherche
 * ({@code GET /events/search}) est un endpoint distinct, non présent dans {@link EventControllerDoc}
 * (CU2, sans documentation Swagger dédiée).
 * <p>
 * {@code update}/{@code delete} : en plus du rôle, l'organisateur ne peut agir que sur les évènements
 * dont il est propriétaire → contrôle de <strong>propriété</strong> effectué dans le service (403 sinon).
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
   * Service de génération des fiches événement au format PDF.
   */
  private final EventPdfService eventPdfService;

  /**
   * Initialise le contrôleur avec les services de gestion des événements,
   * des images, des commentaires et de génération PDF.
   *
   * @param eventService    service de gestion des événements
   * @param eventPdfService service de génération des fiches événement au format PDF
   * @param imageService    service de gestion de la galerie photos des événements
   * @param commentService  service de gestion des commentaires des événements
   */
  public EventController(
      EventService eventService,
      EventPdfService eventPdfService,
      ImageService imageService,
      CommentService commentService
  ) {
    this.eventService = eventService;
    this.eventPdfService = eventPdfService;
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

  /**
   * Télécharge la fiche détaillée d'un événement au format PDF.
   * <p>
   * Les règles d'accès à l'événement sont les mêmes que pour
   * la consultation de son détail.
   * </p>
   *
   * @param id identifiant de l'événement
   * @return fichier PDF de la fiche détaillée de l'événement
   */
  @Override
  @GetMapping(
      value = "/{id}/pdf",
      produces = MediaType.APPLICATION_PDF_VALUE
  )
  public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {

    byte[] pdf = eventPdfService.generatePdf(id);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"event-" + id + ".pdf\""
        )
        .body(pdf);
  }

  /**
   * Recherche les événements publiés selon des critères multiples (CU2), tous facultatifs.
   *
   * @param category  catégorie recherchée
   * @param minPrice  tarif non affilié minimum
   * @param maxPrice  tarif non affilié maximum
   * @param location  lieu recherché
   * @param startDate borne de début de la période
   * @param endDate   borne de fin de la période
   * @param keywords  mots-clés recherchés dans le titre ou la description
   * @return les événements correspondants, sous forme de cartes
   */
  @GetMapping("/search")
  public List<EventCardDto> search(
      @RequestParam(required = false) Category category,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) String keywords
  ) {
    EventSearchCriteria criteria = new EventSearchCriteria(
        category, minPrice, maxPrice, location, startDate, endDate, keywords
    );
    return eventService.searchEvents(criteria);
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
  @PostMapping("/{id}/cancel")
  public EventDto cancel(@PathVariable Long id)
      throws FunctionalException {

    return eventService.cancelEvent(id);
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
  @Secured("ROLE_ORGANIZER")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) throws FunctionalException {
    eventService.deleteEvent(id);
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
