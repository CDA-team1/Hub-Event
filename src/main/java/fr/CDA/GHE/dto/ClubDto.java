package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;

import java.time.LocalDate;

/**
 * Représentation d'un club exposée par l'API.
 * <p>
 * {@code validityEndDate} n'est jamais saisissable en entrée (création/modification) :
 * elle est calculée par le système et ignorée si elle est fournie dans le body.
 * </p>
 *
 * @param id              identifiant du club (ignoré en création)
 * @param name            nom du club
 * @param category        catégorie du club
 * @param postalAddress   adresse postale du club
 * @param email           adresse email du club
 * @param phone           numéro de téléphone du club
 * @param validityEndDate date de fin de validité de l'affiliation (calculée, {@code null} tant que le club est actif)
 */
public record ClubDto(
        Long id,
        String name,
        Category category,
        String postalAddress,
        String email,
        String phone,
        LocalDate validityEndDate
) {
}
