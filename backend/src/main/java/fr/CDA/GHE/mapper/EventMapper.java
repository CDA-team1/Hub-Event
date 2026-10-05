package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.EventDto;
import fr.CDA.GHE.dto.EventCardDto;
import fr.CDA.GHE.dto.EventDetailResponse;
import fr.CDA.GHE.dto.ImageDto;
import fr.CDA.GHE.dto.MyRegistrationDto;
import fr.CDA.GHE.entity.Event;
import org.springframework.stereotype.Component;

import java.util.List;

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
   * @param imageUrl URL de l'image de couverture, {@code null} si aucune image (EVT-12)
   * @return DTO correspondant à l'événement
   */
  public EventCardDto toCardDto(Event event, String imageUrl) {
    return new EventCardDto(
        event.getId(),
        event.getTitle(),
        event.getLocation(),
        event.getStartDateTime(),
        event.getEndDateTime(),
        event.getAffiliatedPrice(),
        event.getNonAffiliatedPrice(),
        event.getCategory(),
        imageUrl
    );
  }

  /**
   * Convertit un événement en DTO de détail.
   *
   * @param event          événement à convertir
   * @param remainingSeats places encore disponibles
   * @param waitingCount   nombre de personnes en liste d'attente
   * @param owner          {@code true} si l'utilisateur connecté est l'organisateur
   * @param myRegistration inscription de l'utilisateur connecté, {@code null} si aucune
   * @param gallery        galerie complète des images de l'événement (EVT-12)
   * @return DTO contenant les informations détaillées de l'événement
   */
  public EventDetailResponse toDetailResponse(
          Event event,
          long remainingSeats,
          long waitingCount,
          boolean owner,
          MyRegistrationDto myRegistration,
          List<ImageDto> gallery
  ) {
    return new EventDetailResponse(
            event.getTitle(),
            event.getDescription(),
            event.getLocation(),
            event.getStartDateTime(),
            event.getEndDateTime(),
            event.getAffiliatedPrice(),
            event.getNonAffiliatedPrice(),
            event.getMaxSeats(),
            event.getCategory(),
            event.getStatus(),
            remainingSeats,
            waitingCount,
            owner,
            myRegistration,
            gallery
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
