package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.EventListDto;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.EventMapper;
import fr.CDA.GHE.repository.EventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Service gérant la consultation des événements.
 */
@Service
public class EventService {

  /**
   * Repository permettant l'accès aux événements en base de données.
   */
  private final EventRepository eventRepository;

  /**
   * Mapper utilisé pour convertir les événements en DTO.
   */
  private final EventMapper eventMapper;

  /**
   * Initialise le service avec ses dépendances.
   *
   * @param eventRepository repository d'accès aux événements
   * @param eventMapper     mapper permettant de convertir un événement en DTO
   */
  public EventService(EventRepository eventRepository, EventMapper eventMapper) {
    this.eventRepository = eventRepository;
    this.eventMapper = eventMapper;
  }

  /**
   * Retourne les événements destinés à la page publique de consultation.
   * <p>
   * Les événements publiés sont regroupés par catégorie et triés
   * chronologiquement par date de début.
   * Les événements terminés sont placés dans la rubrique des événements passés.
   * </p>
   *
   * @return les différentes listes d'événements à afficher
   */
  @Transactional(readOnly = true)
  public EventListDto getPublicEvents() {

    List<Event> publishedEvents =
        eventRepository.findByStatus(EventStatus.PUBLISHED);

    List<Event> finishedEvents =
        eventRepository.findByStatus(EventStatus.FINISHED);

    List<EventCardDto> cultureEvents =
        filterAndSortByCategory(publishedEvents, Category.CULTURE);

    List<EventCardDto> leisureEvents =
        filterAndSortByCategory(publishedEvents, Category.LEISURE);

    List<EventCardDto> sportEvents =
        filterAndSortByCategory(publishedEvents, Category.SPORT);

    List<EventCardDto> pastEvents = finishedEvents.stream()
        .sorted(Comparator.comparing(Event::getStartDateTime))
        .map(eventMapper::toCardDto)
        .toList();

    return new EventListDto(
        cultureEvents,
        leisureEvents,
        sportEvents,
        pastEvents
    );
  }

  /**
   * Retourne le détail d'un événement accessible à l'utilisateur courant.
   * <p>
   * Les événements publiés et terminés sont accessibles publiquement.
   * Un événement brouillon est accessible uniquement à son organisateur.
   * Les autres événements sont considérés comme non accessibles.
   * </p>
   *
   * @param id identifiant de l'événement recherché
   * @return le détail de l'événement
   * @throws NotFoundException si l'événement n'existe pas ou n'est pas accessible
   */
  @Transactional(readOnly = true)
  public EventDetailResponse getEventDetail(Long id) {

    // TODO EVT-03 : utiliser @EntityGraph ou JOIN FETCH pour charger
    // les inscriptions, les images et les commentaires lorsque ces relations
    // seront disponibles dans Event.

    Event event = eventRepository.findById(id)
        .orElseThrow(() ->
            new NotFoundException("Événement introuvable")
        );

    if (!isEventAccessible(event)) {
      throw new NotFoundException("Événement introuvable");
    }

    return eventMapper.toDetailResponse(event);
  }

  /**
   * Filtre les événements selon une catégorie, les trie par date de début
   * puis les convertit en DTO de carte.
   *
   * @param events   événements à traiter
   * @param category catégorie recherchée
   * @return les événements de la catégorie, triés chronologiquement
   */
  private List<EventCardDto> filterAndSortByCategory(
      List<Event> events,
      Category category
  ) {
    return events.stream()
        .filter(event -> event.getCategory() == category)
        .sorted(Comparator.comparing(Event::getStartDateTime))
        .map(eventMapper::toCardDto)
        .toList();
  }

  /**
   * Vérifie si un événement peut être consulté par l'utilisateur courant.
   *
   * @param event événement dont la visibilité doit être vérifiée
   * @return {@code true} si l'événement est accessible, sinon {@code false}
   */
  private boolean isEventAccessible(Event event) {

    if (event.getStatus() == EventStatus.PUBLISHED
        || event.getStatus() == EventStatus.FINISHED) {
      return true;
    }

    if (event.getStatus() != EventStatus.DRAFT) {
      return false;
    }

    Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !(authentication.getPrincipal() instanceof User authenticatedUser)) {
      return false;
    }

    return authenticatedUser.getId().equals(event.getOrganizer().getId());
  }
}
