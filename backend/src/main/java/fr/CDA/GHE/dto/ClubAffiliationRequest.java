package fr.CDA.GHE.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Nouvelle liste de clubs affiliés soumise par un administrateur (CU26, SFG §2.29).
 * <p>
 * Remplace l'ensemble des affiliations existantes du membre : les clubs absents de cette
 * liste sont retirés, ceux qui y figurent et ne l'étaient pas encore sont ajoutés. Une liste
 * vide est valide : le membre devient alors non affilié (règle métier n°5).
 *
 * @param clubIds identifiants des clubs auxquels le membre doit être affilié
 */
public record ClubAffiliationRequest(
        @NotNull
        List<@Positive Long> clubIds
) {
}
