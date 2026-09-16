package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.AnonymizationRequest;
import fr.CDA.GHE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link AnonymizationRequest}.
 */
public interface AnonymizationRequestRepository extends JpaRepository<AnonymizationRequest, Long> {

    /**
     * Recherche la demande d'anonymisation d'un utilisateur (au plus une par utilisateur).
     *
     * @param user l'utilisateur concerné
     * @return la demande correspondante, si elle existe
     */
    Optional<AnonymizationRequest> findByUser(User user);
}
