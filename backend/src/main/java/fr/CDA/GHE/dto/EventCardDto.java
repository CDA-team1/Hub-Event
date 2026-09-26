package fr.CDA.GHE.dto;

import fr.CDA.GHE.entity.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO représentant un événement sous forme de carte
 * dans les listes publiques d'événements.
 * <p>
 * TODO : ajouter une image de couverture — la gestion des images est déjà implémentée
 * (ImageRepository/ImageService), mais la règle de sélection de l'image principale parmi
 * la galerie d'un événement reste à définir.
 * <p>
 * TODO : confirmer si la description doit être affichée sur les cartes
 * avant de l'ajouter à ce DTO.
 *
 * @param id                 identifiant de l'événement
 * @param title              titre de l'événement
 * @param location           lieu de l'événement
 * @param startDateTime      date et heure de début
 * @param endDateTime        date et heure de fin, éventuellement null
 * @param affiliatedPrice    tarif pour les membres affiliés
 * @param nonAffiliatedPrice tarif pour les membres non affiliés
 * @param category           catégorie de l'événement
 */
public record EventCardDto(
    Long id,
    String title,
    String location,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime,
    BigDecimal affiliatedPrice,
    BigDecimal nonAffiliatedPrice,
    Category category
) {
}