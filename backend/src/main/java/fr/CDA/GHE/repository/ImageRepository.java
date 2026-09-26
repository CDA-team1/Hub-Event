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
}
