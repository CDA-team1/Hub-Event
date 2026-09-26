package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO représentant un événement renvoyé par l'API.
 *
 * @param id                 identifiant de l'événement
 * @param title              titre de l'événement
 * @param description        description de l'événement
 * @param location           lieu de l'événement
 * @param startDateTime      date et heure de début
 * @param endDateTime        date et heure de fin, éventuellement {@code null}
 * @param affiliatedPrice    tarif pour les participants affiliés
 * @param nonAffiliatedPrice tarif pour les participants non affiliés
 * @param maxSeats           nombre maximal de places
 * @param status             statut de l'événement
 * @param category           catégorie de l'événement
 * @param organizerId        identifiant de l'organisateur
 * @param clubId             identifiant du club organisateur
 */
public record EventDto(
    Long id,
    String title,
    String description,
    String location,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime,
    BigDecimal affiliatedPrice,
    BigDecimal nonAffiliatedPrice,
    Integer maxSeats,
    EventStatus status,
    Category category,
    Long organizerId,
    Long clubId
) {
}