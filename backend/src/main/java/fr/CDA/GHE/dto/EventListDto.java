package fr.CDA.GHE.dto;

import java.util.List;

/**
 * DTO représentant les différentes listes d'événements
 * affichées sur la page publique de consultation.
 *
 * @param cultureEvents événements culturels publiés
 * @param leisureEvents événements de loisirs publiés
 * @param sportEvents   événements sportifs publiés
 * @param pastEvents    événements terminés
 */
public record EventListDto(
    List<EventCardDto> cultureEvents,
    List<EventCardDto> leisureEvents,
    List<EventCardDto> sportEvents,
    List<EventCardDto> pastEvents
) {
}