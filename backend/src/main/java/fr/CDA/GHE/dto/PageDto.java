package fr.CDA.GHE.dto;

import java.util.List;

/**
 * Enveloppe générique de pagination renvoyée par les endpoints paginés.
 * <p>
 * On n'expose pas directement l'objet {@link org.springframework.data.domain.Page}
 * de Spring Data dans l'API : il contient beaucoup de métadonnées internes et son
 * format JSON est instable d'une version à l'autre. Ce record fige un contrat simple
 * et stable pour le front.
 *
 * @param content       le contenu de la page courante
 * @param page          numéro de la page courante (0-indexé)
 * @param size          taille de page demandée
 * @param totalElements nombre total d'éléments, toutes pages confondues
 * @param totalPages    nombre total de pages
 * @param first         vrai s'il s'agit de la première page
 * @param last          vrai s'il s'agit de la dernière page
 * @param <T>           type des éléments paginés (ex. ClubDto)
 */
public record PageDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
