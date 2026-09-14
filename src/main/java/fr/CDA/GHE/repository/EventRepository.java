package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Repository permettant l'accès aux événements en base de données.
 * <p>
 * Il fournit les opérations CRUD standards de {@link JpaRepository},
 * ainsi que la possibilité d'effectuer des recherches dynamiques
 * grâce à {@link JpaSpecificationExecutor}.
 * </p>
 */
public interface EventRepository
    extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

  /**
   * Recherche les événements possédant un statut donné.
   *
   * @param status statut des événements recherchés
   * @return la liste des événements correspondant au statut
   */
  List<Event> findByStatus(EventStatus status);

  /**
   * Recherche les événements correspondant à une catégorie
   * et à un statut donnés.
   *
   * @param category catégorie des événements recherchés
   * @param status   statut des événements recherchés
   * @return la liste des événements correspondant à la catégorie et au statut
   */
  List<Event> findByCategoryAndStatus(Category category, EventStatus status);

  /**
   * Recherche les événements organisés par un utilisateur donné.
   *
   * @param organizer utilisateur organisateur
   * @return la liste des événements organisés par cet utilisateur
   */
  List<Event> findByOrganizer(User organizer);

  /**
   * Recherche les événements appartenant à un club donné.
   *
   * @param clubId identifiant du club
   * @return la liste des événements appartenant à ce club
   */
  List<Event> findByClub_Id(Long clubId);
}