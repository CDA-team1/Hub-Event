package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Données saisies par un organisateur pour créer un événement.
 * <p>
 * Le statut et l'organisateur ne sont pas fournis par le client :
 * le statut est automatiquement défini à DRAFT et l'organisateur
 * correspond à l'utilisateur actuellement authentifié.
 * </p>
 * <p>
 * Le club est choisi parmi les clubs auxquels l'organisateur est affilié.
 * Le service vérifie que le club demandé appartient bien à ses affiliations.
 * </p>
 *
 * @param title              titre de l'événement, obligatoire et non vide
 * @param description        description de l'événement, obligatoire et non vide
 * @param location           lieu de l'événement, obligatoire et non vide
 * @param startDateTime      date et heure de début, obligatoires et futures
 * @param endDateTime        date et heure de fin, éventuellement {@code null},
 *                           mais postérieures au début lorsqu'elles sont renseignées
 * @param affiliatedPrice    tarif pour les participants affiliés, supérieur ou égal à zéro
 * @param nonAffiliatedPrice tarif pour les participants non affiliés, supérieur ou égal à zéro
 * @param maxSeats           nombre maximal de places, strictement supérieur à zéro
 * @param category           catégorie de l'événement, obligatoire
 * @param clubId             identifiant du club organisateur, obligatoire
 */
public record CreateEventRequest(
    String title,
    String description,
    String location,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime,
    BigDecimal affiliatedPrice,
    BigDecimal nonAffiliatedPrice,
    Integer maxSeats,
    Category category,
    Long clubId
) {
}