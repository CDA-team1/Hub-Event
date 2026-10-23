package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link Comment}.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Recherche les commentaires d'un événement, du plus ancien au plus récent (règle
     * métier n°3 : affichage dans la liste des commentaires de l'événement).
     *
     * @param eventId identifiant de l'événement
     * @return la liste des commentaires de l'événement, triée chronologiquement
     */
    List<Comment> findByEvent_IdOrderByCreatedAtAsc(Long eventId);

    /**
     * Supprime tous les commentaires d'un événement (CU24/EVT-13 : la suppression physique
     * d'un événement — brouillon, ou publié sans inscrit — est définitive et emporte donc
     * aussi ses commentaires, sans quoi la contrainte de clé étrangère {@code comments.event_id}
     * empêcherait la suppression de l'événement).
     *
     * @param eventId identifiant de l'événement
     */
    void deleteAllByEvent_Id(Long eventId);
}
