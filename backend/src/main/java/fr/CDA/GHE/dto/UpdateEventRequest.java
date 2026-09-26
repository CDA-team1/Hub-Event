package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Données permettant de modifier un événement.
 *
 * @param title              titre de l'événement
 * @param description        description de l'événement
 * @param location           lieu de l'événement
 * @param startDateTime      date et heure de début
 * @param endDateTime        date et heure de fin, éventuellement {@code null}
 * @param affiliatedPrice    tarif pour les participants affiliés
 * @param nonAffiliatedPrice tarif pour les participants non affiliés
 * @param maxSeats           nombre maximal de places
 * @param category           catégorie de l'événement
 */
public record UpdateEventRequest( String title,
                                  String description,
                                  String location,
                                  LocalDateTime startDateTime,
                                  LocalDateTime endDateTime,
                                  BigDecimal affiliatedPrice,
                                  BigDecimal nonAffiliatedPrice,
                                  Integer maxSeats,
                                  Category category
) {
}
