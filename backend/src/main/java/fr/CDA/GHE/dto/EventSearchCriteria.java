package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Critères de recherche multicritère des événements (CU2).
 * <p>
 * Tous les champs sont facultatifs : seuls les critères effectivement renseignés
 * sont appliqués, en complément du filtre obligatoire sur les événements
 * {@code PUBLISHED}.
 * </p>
 *
 * @param category  catégorie recherchée
 * @param minPrice  tarif non affilié minimum
 * @param maxPrice  tarif non affilié maximum
 * @param location  lieu recherché (recherche partielle, insensible à la casse)
 * @param startDate borne de début de la période recherchée
 * @param endDate   borne de fin de la période recherchée
 * @param keywords  mots-clés recherchés dans le titre ou la description
 */
public record EventSearchCriteria (
        Category category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        String keywords
){
}
