package fr.CDA.GHE.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO représentant le détail d'un événement consultable.
 *
 * @param title              titre de l'événement
 * @param description        description détaillée de l'événement
 * @param location           lieu de l'événement
 * @param startDateTime      date et heure de début
 * @param endDateTime        date et heure de fin, éventuellement {@code null}
 * @param affiliatedPrice    tarif pour les membres affiliés
 * @param nonAffiliatedPrice tarif pour les membres non affiliés
 * @param maxSeats           nombre maximal de places de l'événement
 */
public record EventDetailResponse(
    String title,
    String description,
    String location,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime,
    BigDecimal affiliatedPrice,
    BigDecimal nonAffiliatedPrice,
    Integer maxSeats
) {

  // TODO EVT-03 : ajouter remainingSeats et waitingCount
  // lorsque REG-01 sera intégré dans dev.

  // TODO EVT-03 : ajouter gallery
  // lorsque la gestion des images sera implémentée.

  // TODO EVT-03 : ajouter comments
  // lorsque la gestion des commentaires sera implémentée.
}