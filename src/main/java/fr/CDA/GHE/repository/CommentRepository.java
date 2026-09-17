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
}
