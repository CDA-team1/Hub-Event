package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.EventCardDto;
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
}
