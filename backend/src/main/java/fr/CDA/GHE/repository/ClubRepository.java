package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link Club}.
 */
public interface ClubRepository extends JpaRepository<Club, Long> {

  /**
   * Recherche les clubs auxquels un utilisateur est affilié.
   *
   * @param userId identifiant de l'utilisateur
   * @return la liste des clubs auxquels l'utilisateur est affilié
   */
  List<Club> findByMembers_Id(Long userId);

  /**
   * Recherche les clubs dont la date de fin de validité n'est pas renseignée.
   *
   * @param pageable pagination demandée
   * @return la page de clubs actuellement affiliés
   */
  Page<Club> findByValidityEndDateIsNull(Pageable pageable);
}