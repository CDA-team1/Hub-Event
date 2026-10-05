package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;

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
 * @param category           catégorie de l'événement
 * @param status             statut de l'événement
 * @param remainingSeats     nombre de places encore disponibles, jamais négatif
 * @param waitingCount       nombre de personnes en liste d'attente
 * @param owner              {@code true} si l'utilisateur connecté est l'organisateur de l'événement
 * @param myRegistration     inscription de l'utilisateur connecté, {@code null} s'il est anonyme
 *                           ou non inscrit
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
        Category category,
        EventStatus status,
        long remainingSeats,
        long waitingCount,
        boolean owner,
        MyRegistrationDto myRegistration,
        List<ImageDto> gallery
) {
}