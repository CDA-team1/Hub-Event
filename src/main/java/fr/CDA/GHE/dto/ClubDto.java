package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;

import java.time.LocalDate;
import java.util.List;

/**
 * Représentation d'un club exposée par l'API.
 * <p>
 * {@code validityEndDate} n'est jamais saisissable en entrée (création/modification) :
 * elle est calculée par le système et ignorée si elle est fournie dans le body.
 * {@code members} n'est jamais saisissable non plus : l'affiliation se gère via une
 * fonctionnalité dédiée (CU26/27), pas via le CRUD club.
 * </p>
 *
 * @param id              identifiant du club (ignoré en création)
 * @param name            nom du club
 * @param category        catégorie du club
 * @param postalAddress   adresse postale du club
 * @param email           adresse email du club
 * @param phone           numéro de téléphone du club
 * @param validityEndDate date de fin de validité de l'affiliation (calculée, {@code null} tant que le club est actif)
 * @param members         membres actuellement affiliés au club
 */
public record ClubDto(
        Long id,
        String name,
        Category category,
        String postalAddress,
        String email,
        String phone,
        LocalDate validityEndDate,
        List<MemberSummaryDto> members
) {
}
