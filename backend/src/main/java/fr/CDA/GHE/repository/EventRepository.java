package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link Event}.
 * <p>
 * Il fournit les opérations CRUD standards de {@link JpaRepository},
 * ainsi que la possibilité d'effectuer des recherches dynamiques
 * grâce à {@link JpaSpecificationExecutor}.
 * </p>
 */
public interface EventRepository
    extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

  /**
   * Charge un événement en posant un verrou en écriture sur sa ligne jusqu'à la fin de la
   * transaction courante.
   * <p>
   * À utiliser pour toute opération qui lit puis modifie le nombre de places occupées
   * (inscription, désinscription, promotion depuis la liste d'attente) : deux requêtes
   * simultanées sur le même événement s'exécutent alors l'une après l'autre, au lieu de lire
   * toutes deux « il reste une place » et de dépasser la capacité.
   * </p>
   *
   * @param id identifiant de l'événement
   * @return l'événement verrouillé, vide s'il n'existe pas
   */
  // Requête native pour conserver le verrou FOR UPDATE sans générer la syntaxe incompatible "FOR UPDATE OF".
  @Query(
    value = "SELECT * FROM events WHERE id = :id FOR UPDATE",
    nativeQuery = true
)
  Optional<Event> findByIdForUpdate(@Param("id") Long id);

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

  /**
   * Recherche les événements d'un organisateur possédant un statut donné (CU29 : annulation
   * des événements publiés à venir lors de l'anonymisation d'un organisateur).
   *
   * @param organizer utilisateur organisateur
   * @param status    statut des événements recherchés
   * @return la liste des événements correspondants
   */
  List<Event> findByOrganizerAndStatus(User organizer, EventStatus status);
}