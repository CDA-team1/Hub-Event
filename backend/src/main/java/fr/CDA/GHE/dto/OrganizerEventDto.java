package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;

import java.time.LocalDateTime;

/**
 * DTO représentant un événement dans la page "Mes événements" de l'organisateur (EVT-05).
 * <p>
 * Contrairement à {@link EventCardDto}, utilisé pour les listes publiques, tous les statuts
 * sont représentés ici (y compris DRAFT et CANCELLED), et le nombre de places occupées est
 * inclus pour l'affichage de la colonne "Places".
 *
 * @param id              identifiant de l'événement
 * @param title           titre de l'événement
 * @param category        catégorie de l'événement
 * @param startDateTime   date et heure de début
 * @param endDateTime     date et heure de fin, éventuellement {@code null}
 * @param status          statut de l'événement
 * @param maxSeats        nombre maximal de places
 * @param registeredCount nombre de places occupées (inscriptions au statut REGISTERED)
 */
public record OrganizerEventDto(
    Long id,
    String title,
    Category category,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime,
    EventStatus status,
    Integer maxSeats,
    long registeredCount
) {
}
