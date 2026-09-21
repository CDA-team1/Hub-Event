package fr.CDA.GHE.entity;

import fr.CDA.GHE.entity.enums.RegistrationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entité représentant l'inscription d'un utilisateur à un événement.
 * <p>
 * Un utilisateur ne peut être inscrit qu'une seule fois à un même événement
 * (contrainte d'unicité sur le couple utilisateur/événement). Selon les
 * places disponibles au moment de l'inscription, son statut est soit
 * {@link RegistrationStatus#REGISTERED}, soit {@link RegistrationStatus#WAITING_LIST}.
 * </p>
 */
@Entity
@Table(
        name = "registrations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"})
)
public class Registration {

  /**
   * Identifiant unique de l'inscription.
   * Généré automatiquement par la base de données.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Utilisateur inscrit.
   * <p>
   * Registration est le côté propriétaire de cette relation grâce à la clé étrangère
   * {@code user_id}.
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * Événement concerné par l'inscription.
   * <p>
   * Registration est le côté propriétaire de cette relation grâce à la clé étrangère
   * {@code event_id}.
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  /**
   * Statut actuel de l'inscription.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RegistrationStatus status;

  /**
   * Date et heure de l'inscription.
   */
  @Column(name = "registration_date", nullable = false)
  private LocalDateTime registrationDate;

  /**
   * Constructeur vide requis par JPA.
   */
  public Registration() {
  }

  /**
   * Initialise une inscription avec toutes ses informations.
   *
   * @param user             utilisateur inscrit
   * @param event            événement concerné
   * @param status           statut de l'inscription
   * @param registrationDate date et heure de l'inscription
   */
  public Registration(User user, Event event, RegistrationStatus status, LocalDateTime registrationDate) {
    this.user = user;
    this.event = event;
    this.status = status;
    this.registrationDate = registrationDate;
  }

  /**
   * Retourne l'identifiant unique de l'inscription.
   *
   * @return l'identifiant de l'inscription
   */
  public Long getId() {
    return id;
  }

  /**
   * Retourne l'utilisateur inscrit.
   *
   * @return l'utilisateur inscrit
   */
  public User getUser() {
    return user;
  }

  /**
   * Modifie l'utilisateur inscrit.
   *
   * @param user nouvel utilisateur inscrit
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Retourne l'événement concerné par l'inscription.
   *
   * @return l'événement concerné
   */
  public Event getEvent() {
    return event;
  }

  /**
   * Modifie l'événement concerné par l'inscription.
   *
   * @param event nouvel événement concerné
   */
  public void setEvent(Event event) {
    this.event = event;
  }

  /**
   * Retourne le statut actuel de l'inscription.
   *
   * @return le statut de l'inscription
   */
  public RegistrationStatus getStatus() {
    return status;
  }

  /**
   * Modifie le statut de l'inscription.
   *
   * @param status nouveau statut de l'inscription
   */
  public void setStatus(RegistrationStatus status) {
    this.status = status;
  }

  /**
   * Retourne la date et l'heure de l'inscription.
   *
   * @return la date et l'heure de l'inscription
   */
  public LocalDateTime getRegistrationDate() {
    return registrationDate;
  }

  /**
   * Modifie la date et l'heure de l'inscription.
   *
   * @param registrationDate nouvelle date et heure de l'inscription
   */
  public void setRegistrationDate(LocalDateTime registrationDate) {
    this.registrationDate = registrationDate;
  }
}