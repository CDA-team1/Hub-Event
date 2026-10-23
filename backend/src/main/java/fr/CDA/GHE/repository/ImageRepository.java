package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link Image}.
 */
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Recherche les images associées à un événement donné.
     *
     * @param eventId identifiant de l'événement
     * @return la liste des images de la galerie de l'événement
     */
    List<Image> findByEvent_Id(Long eventId);

    /**
     * Recherche les images associées à plusieurs événements à la fois (évite le problème
     * des N+1 requêtes lors de l'affichage d'une liste d'événements).
     *
     * @param eventIds identifiants des événements concernés
     * @return la liste des images appartenant à l'un de ces événements
     */
    List<Image> findByEvent_IdIn(List<Long> eventIds);

    /**
     * Recherche les images de prévisualisation (preview) de plusieurs événements à la fois.
     *
     * @param eventIds identifiants des événements concernés
     * @return les images preview appartenant à l'un de ces événements
     */
    List<Image> findByEvent_IdInAndIsPreviewTrue(List<Long> eventIds);

    /**
     * Indique si un événement possède déjà une image de prévisualisation.
     *
     * @param eventId identifiant de l'événement
     * @return {@code true} si une image de cet événement est marquée preview
     */
    boolean existsByEvent_IdAndIsPreviewTrue(Long eventId);
}
