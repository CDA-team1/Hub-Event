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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RegistrationStatus status;

  @Column(name = "registration_date", nullable = false)
  private LocalDateTime registrationDate;

  public Registration() {
  }

  public Registration(User user, Event event, RegistrationStatus status, LocalDateTime registrationDate) {
    this.user = user;
    this.event = event;
    this.status = status;
    this.registrationDate = registrationDate;
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  public RegistrationStatus getStatus() {
    return status;
  }

  public void setStatus(RegistrationStatus status) {
    this.status = status;
  }

  public LocalDateTime getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(LocalDateTime registrationDate) {
    this.registrationDate = registrationDate;
  }
}