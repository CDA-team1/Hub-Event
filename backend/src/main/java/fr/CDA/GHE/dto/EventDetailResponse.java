package fr.CDA.GHE.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
 * @param gallery            galerie complète des images de l'événement (EVT-12)
 */
public record EventDetailResponse(
    String title,
    String description,
    String location,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime,
    BigDecimal affiliatedPrice,
    BigDecimal nonAffiliatedPrice,
    Integer maxSeats,
    List<ImageDto> gallery
) {

  // TODO EVT-03 : ajouter remainingSeats et waitingCount — les inscriptions (REG-01) sont
  // déjà intégrées côté données (RegistrationRepository), il reste à les exposer ici.

  // TODO EVT-03 : ajouter comments — la gestion des commentaires est déjà implémentée
  // (CommentRepository/CommentService), il reste à l'exposer ici.
}