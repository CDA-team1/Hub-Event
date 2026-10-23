package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link Registration}.
 */
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

  /**
   * Recherche toutes les inscriptions à un évènement donné.
   *
   * @param event l'évènement concerné
   * @return les inscriptions à cet évènement
   */
  List<Registration> findByEvent(Event event);

  /**
   * Recherche toutes les inscriptions d'un utilisateur donné.
   *
   * @param user l'utilisateur concerné
   * @return les inscriptions de cet utilisateur
   */
  List<Registration> findByUser(User user);

  /**
   * Compte les inscriptions à un évènement possédant un statut donné (ex. places occupées).
   *
   * @param event  l'évènement concerné
   * @param status le statut recherché
   * @return le nombre d'inscriptions correspondantes
   */
  long countByEventAndStatus(Event event, RegistrationStatus status);

  /**
   * Vérifie si un utilisateur est déjà inscrit à un évènement donné.
   *
   * @param user  l'utilisateur concerné
   * @param event l'évènement concerné
   * @return {@code true} si une inscription existe déjà pour ce couple
   */
  boolean existsByUserAndEvent(User user, Event event);

  /**
   * Recherche l'inscription d'un utilisateur à un évènement donné.
   *
   * @param user  l'utilisateur concerné
   * @param event l'évènement concerné
   * @return l'inscription correspondante, si elle existe
   */
  Optional<Registration> findByUserAndEvent(User user, Event event);

  /**
   * Recherche la plus ancienne inscription en liste d'attente pour un évènement donné
   * (le premier de la file, à promouvoir en priorité).
   *
   * @param event  l'évènement concerné
   * @param status le statut recherché (typiquement {@code WAITING_LIST})
   * @return la première inscription en liste d'attente, si elle existe
   */
  Optional<Registration> findFirstByEventAndStatusOrderByRegistrationDateAsc(Event event, RegistrationStatus status);

  /**
   * Compte les inscriptions d'un statut donné effectuées avant une date.
   * Sert à calculer la position en liste d'attente : nombre d'inscrits devant soi + 1.
   *
   * @param event  l'évènement concerné
   * @param status le statut recherché (typiquement {@code WAITING_LIST})
   * @param date   la date d'inscription de référence (exclue)
   * @return le nombre d'inscriptions plus anciennes
   */
  long countByEventAndStatusAndRegistrationDateBefore(Event event, RegistrationStatus status, LocalDateTime date);

  /**
   * Recherche les inscriptions confirmées d'un utilisateur dont l'événement chevauche
   * la période demandée (CAL-01).
   *
   * @param user l'utilisateur concerné
   * @param from début de la période recherchée
   * @param to   fin de la période recherchée
   * @return les inscriptions REGISTERED correspondantes
   */
  @Query("SELECT r FROM Registration r " +
          "WHERE r.user = :user " +
          "AND r.status = fr.CDA.GHE.entity.enums.RegistrationStatus.REGISTERED " +
          "AND r.event.startDateTime <= :to " +
          "AND COALESCE(r.event.endDateTime, r.event.startDateTime) >= :from")

  List<Registration> findRegisteredByUserAndPeriod(
          @Param("user") User user,
          @Param("from") LocalDateTime from,
          @Param("to") LocalDateTime to
          );
}