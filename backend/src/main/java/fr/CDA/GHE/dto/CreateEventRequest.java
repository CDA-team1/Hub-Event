package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        String description,

        @NotBlank
        @Size(max = 255)
        String location,

        @NotNull
        @Future
        LocalDateTime startDateTime,

        LocalDateTime endDateTime,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal affiliatedPrice,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal nonAffiliatedPrice,

        @NotNull
        @Positive
        Integer maxSeats,

        @NotNull
        Category category,

        @NotNull
        @Positive
        Long clubId
) {
}
