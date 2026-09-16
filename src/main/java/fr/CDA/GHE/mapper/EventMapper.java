package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.entity.Event;
import org.springframework.stereotype.Component;

/**
 * Mapper chargé de convertir les entités {@link Event}
 * en DTO destinés à l'affichage.
 */
@Component
public class EventMapper {


  /**
   * Convertit un événement en DTO de carte.
   *
   * @param event événement à convertir
   * @return DTO correspondant à l'événement
   */
  public EventCardDto toCardDto(Event event) {
    return new EventCardDto(
        event.getId(),
        event.getTitle(),
        event.getLocation(),
        event.getStartDateTime(),
        event.getEndDateTime(),
        event.getAffiliatedPrice(),
        event.getNonAffiliatedPrice(),
        event.getCategory()
    );
  }

  /**
   * Convertit un événement en DTO de détail.
   *
   * @param event événement à convertir
   * @return DTO contenant les informations détaillées de l'événement
   */
  public EventDetailResponse toDetailResponse(Event event) {
    return new EventDetailResponse(
        event.getTitle(),
        event.getDescription(),
        event.getLocation(),
        event.getStartDateTime(),
        event.getEndDateTime(),
        event.getAffiliatedPrice(),
        event.getNonAffiliatedPrice(),
        event.getMaxSeats()
    );
  }

  /**
   * Convertit un événement en DTO destiné aux réponses de l'API.
   *
   * @param event événement à convertir
   * @return DTO correspondant à l'événement
   */
  public EventDto toDto(Event event) {
    return new EventDto(
        event.getId(),
        event.getTitle(),
        event.getDescription(),
        event.getLocation(),
        event.getStartDateTime(),
        event.getEndDateTime(),
        event.getAffiliatedPrice(),
        event.getNonAffiliatedPrice(),
        event.getMaxSeats(),
        event.getStatus(),
        event.getCategory(),
        event.getOrganizer().getId(),
        event.getClub().getId()
    );
  }
}
