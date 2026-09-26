package fr.CDA.GHE.service;

import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Prédicats de recherche multicritère utilisés pour filtrer les événements (CU2).
 * <p>
 * Chaque méthode retourne une {@link Specification} correspondant à un seul critère.
 * Aucune méthode ne gère les valeurs {@code null} : c'est à l'appelant (le service)
 * de n'invoquer une méthode que lorsque le critère correspondant est effectivement
 * renseigné.
 * </p>
 */
public class EventSpecifications {

    private EventSpecifications() {
    }

    /**
     * Filtre les événements dont le statut correspond à celui demandé.
     *
     * @param status statut recherché
     * @return la spécification correspondante
     */
    public static Specification<Event> hasStatus(EventStatus status){
        return (root,query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Filtre les événements dont la catégorie correspond à celle demandée.
     *
     * @param category catégorie recherchée
     * @return la spécification correspondante
     */
    public static Specification<Event> hasCategory(Category category){
        return(root, query, cb) -> cb.equal(root.get("category"), category);
    }

    /**
     * Filtre les événements dont le tarif non affilié est supérieur ou égal
     * au minimum demandé.
     *
     * @param minPrice tarif minimum recherché
     * @return la spécification correspondante
     */
    public static Specification<Event> hasMinPrice(BigDecimal minPrice){
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("nonAffiliatedPrice"), minPrice);
    }

    /**
     * Filtre les événements dont le tarif non affilié est inférieur ou égal
     * au maximum demandé.
     *
     * @param maxPrice tarif maximum recherché
     * @return la spécification correspondante
     */
    public static Specification<Event> hasMaxPrice(BigDecimal maxPrice){
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("nonAffiliatedPrice"), maxPrice);
    }

    /**
     * Filtre les événements dont le lieu contient le texte recherché,
     * indépendamment de la casse.
     *
     * @param location texte recherché dans le lieu
     * @return la spécification correspondante
     */
    public static Specification<Event> hasLocation(String location){
        return (root, query, cb) -> cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    /**
     * Filtre les événements dont la date de début est postérieure ou égale
     * à la borne demandée.
     *
     * @param startOfDay borne de début de la période recherchée
     * @return la spécification correspondante
     */
    public static Specification<Event> startsOnOrAfter(LocalDateTime startOfDay){
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDateTime"), startOfDay);
    }

    /**
     * Filtre les événements dont la date de début est antérieure ou égale
     * à la borne demandée.
     *
     * @param endOfDay borne de fin de la période recherchée
     * @return la spécification correspondante
     */
    public static Specification<Event> startsOnOrBefore(LocalDateTime endOfDay){
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDateTime"), endOfDay);
    }

    /**
     * Filtre les événements dont le titre ou la description contient
     * le mot-clé recherché, indépendamment de la casse.
     *
     * @param keywords mot-clé recherché dans le titre ou la description
     * @return la spécification correspondante
     */
    public static Specification<Event> hasKeywords(String keywords){
        return (root, query, cb) -> {
            String pattern = "%" + keywords.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}
