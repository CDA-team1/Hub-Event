package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
public record UpdateEventRequest( 
        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        String description,

        @NotBlank
        @Size(max = 255)
        String location,

        @NotNull
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
        Category category
) {
}
