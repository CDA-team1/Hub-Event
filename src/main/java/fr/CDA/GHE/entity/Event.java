package fr.CDA.GHE.entity;

import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * Entité représentant un événement proposé au sein du hub événementiel.
 * <p>
 * Un événement appartient obligatoirement à un club et possède un utilisateur
 * organisateur. Il est caractérisé notamment par ses dates, sa localisation,
 * ses tarifs, sa capacité maximale, son statut et sa catégorie.
 * </p>
 */
@Entity
@Table(name = "events")
public class Event {

  /**
   * Identifiant unique de l'événement.
   * Généré automatiquement par la base de données.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Titre de l'événement.
   */
  @Column(nullable = false)
  private String title;

  /**
   * Description détaillée de l'événement.
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  /**
   * Lieu où se déroule l'événement.
   */
  @Column(nullable = false)
  private String location;

  /**
   * Date et heure de début de l'événement.
   */
  @Column(name = "start_date_time", nullable = false)
  private LocalDateTime startDateTime;

  /**
   * Date et heure de fin de l'événement.
   * Peut être {@code null} lorsqu'aucune date de fin n'est définie.
   */
  @Column(name = "end_date_time")
  private LocalDateTime endDateTime;

  /**
   * Tarif appliqué aux participants affiliés.
   */
  @Column(name = "affiliated_price", nullable = false)
  private BigDecimal affiliatedPrice;

  /**
   * Tarif appliqué aux participants non affiliés.
   */
  @Column(name = "non_affiliated_price", nullable = false)
  private BigDecimal nonAffiliatedPrice;

  /**
   * Nombre maximal de places disponibles pour l'événement.
   */
  @Column(name = "max_seats", nullable = false)
  private Integer maxSeats;

  /**
   * Statut actuel de l'événement.
   * Un nouvel événement est créé au statut {@link EventStatus#DRAFT}.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventStatus status;

  /**
   * Catégorie à laquelle appartient l'événement.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Category category;

  /**
   * Utilisateur responsable de l'organisation de l'événement.
   * <p>
   * Event est le côté propriétaire de cette relation grâce à la clé étrangère
   * {@code organizer_id}.
   * </p>
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organizer_id", nullable = false)
  private User organizer;

  /**
   * Club auquel appartient l'événement.
   * <p>
   * Event est le côté propriétaire de cette relation grâce à la clé étrangère
   * {@code club_id}.
   * </p>
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;

  // Pas de relation inverse Event -> Image/Comment/Registration, bien que Image.event,
  // Comment.event et Registration.event existent et soient actifs : aucun code du projet
  // n'a besoin de naviguer depuis un Event vers ses images/commentaires/inscriptions, chaque
  // accès se faisant via une requête dédiée (ImageRepository.findByEvent_Id,
  // CommentRepository.findByEvent_IdOrderByCreatedAtAsc, RegistrationRepository.findByEvent).
  // Même principe qu'ailleurs dans le projet (ex. User n'a pas de collection de Club) : une
  // relation inverse ne s'ajoute qu'avec un consommateur direct, pas par anticipation.

  /**
   * Constructeur vide requis par JPA.
   */
  public Event() {
  }

  /**
   * Initialise un événement avec les informations nécessaires à sa création.
   * Un nouvel événement est automatiquement créé au statut
   * {@link EventStatus#DRAFT}.
   *
   * @param title              titre de l'événement
   * @param description        description détaillée de l'événement
   * @param location           lieu où se déroule l'événement
   * @param startDateTime      date et heure de début de l'événement
   * @param endDateTime        date et heure de fin de l'événement,
   *                           éventuellement {@code null}
   * @param affiliatedPrice    tarif appliqué aux participants affiliés
   * @param nonAffiliatedPrice tarif appliqué aux participants non affiliés
   * @param maxSeats           nombre maximal de places disponibles
   * @param category           catégorie de l'événement
   * @param organizer          utilisateur organisateur de l'événement
   * @param club               club auquel appartient l'événement
   */
  public Event(
      String title,
      String description,
      String location,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      BigDecimal affiliatedPrice,
      BigDecimal nonAffiliatedPrice,
      Integer maxSeats,
      Category category,
      User organizer,
      Club club
  ) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.affiliatedPrice = affiliatedPrice;
    this.nonAffiliatedPrice = nonAffiliatedPrice;
    this.maxSeats = maxSeats;
    this.status = EventStatus.DRAFT;
    this.category = category;
    this.organizer = organizer;
    this.club = club;
  }

  /**
   * Retourne l'identifiant unique de l'événement.
   *
   * @return l'identifiant de l'événement
   */
  public Long getId() {
    return id;
  }

  /**
   * Retourne le titre de l'événement.
   *
   * @return le titre de l'événement
   */
  public String getTitle() {
    return title;
  }

  /**
   * Modifie le titre de l'événement.
   *
   * @param title nouveau titre de l'événement
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Retourne la description de l'événement.
   *
   * @return la description de l'événement
   */
  public String getDescription() {
    return description;
  }

  /**
   * Modifie la description de l'événement.
   *
   * @param description nouvelle description de l'événement
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Retourne le lieu de l'événement.
   *
   * @return le lieu de l'événement
   */
  public String getLocation() {
    return location;
  }

  /**
   * Modifie le lieu de l'événement.
   *
   * @param location nouveau lieu de l'événement
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Retourne la date et l'heure de début de l'événement.
   *
   * @return la date et l'heure de début
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Modifie la date et l'heure de début de l'événement.
   *
   * @param startDateTime nouvelle date et heure de début
   */
  public void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  /**
   * Retourne la date et l'heure de fin de l'événement.
   *
   * @return la date et l'heure de fin, ou {@code null} si aucune date de fin
   * n'est définie
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Modifie la date et l'heure de fin de l'événement.
   *
   * @param endDateTime nouvelle date et heure de fin,
   *                    éventuellement {@code null}
   */
  public void setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
  }

  /**
   * Retourne le tarif appliqué aux participants affiliés.
   *
   * @return le tarif affilié
   */
  public BigDecimal getAffiliatedPrice() {
    return affiliatedPrice;
  }

  /**
   * Modifie le tarif appliqué aux participants affiliés.
   *
   * @param affiliatedPrice nouveau tarif affilié
   */
  public void setAffiliatedPrice(BigDecimal affiliatedPrice) {
    this.affiliatedPrice = affiliatedPrice;
  }

  /**
   * Retourne le tarif appliqué aux participants non affiliés.
   *
   * @return le tarif non affilié
   */
  public BigDecimal getNonAffiliatedPrice() {
    return nonAffiliatedPrice;
  }

  /**
   * Modifie le tarif appliqué aux participants non affiliés.
   *
   * @param nonAffiliatedPrice nouveau tarif non affilié
   */
  public void setNonAffiliatedPrice(BigDecimal nonAffiliatedPrice) {
    this.nonAffiliatedPrice = nonAffiliatedPrice;
  }

  /**
   * Retourne le nombre maximal de places disponibles.
   *
   * @return le nombre maximal de places
   */
  public Integer getMaxSeats() {
    return maxSeats;
  }

  /**
   * Modifie le nombre maximal de places disponibles.
   *
   * @param maxSeats nouveau nombre maximal de places
   */
  public void setMaxSeats(Integer maxSeats) {
    this.maxSeats = maxSeats;
  }

  /**
   * Retourne le statut actuel de l'événement.
   *
   * @return le statut de l'événement
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Publie l'événement.
   * <p>
   * Les contrôles métier et les droits nécessaires à cette transition restent
   * gérés par la couche service.
   * </p>
   */
  public void publish() {
    this.status = EventStatus.PUBLISHED;
  }

  /**
   * Marque l'événement comme terminé.
   * <p>
   * Les contrôles métier et les droits nécessaires à cette transition restent
   * gérés par la couche service.
   * </p>
   */
  public void finish() {
    this.status = EventStatus.FINISHED;
  }

  /**
   * Annule l'événement.
   * <p>
   * Les contrôles métier et les conséquences de l'annulation restent gérés par
   * la couche service.
   * </p>
   */
  public void cancel() {
    this.status = EventStatus.CANCELLED;
  }

  /**
   * Retourne la catégorie de l'événement.
   *
   * @return la catégorie de l'événement
   */
  public Category getCategory() {
    return category;
  }

  /**
   * Modifie la catégorie de l'événement.
   *
   * @param category nouvelle catégorie de l'événement
   */
  public void setCategory(Category category) {
    this.category = category;
  }

  /**
   * Retourne l'utilisateur organisateur de l'événement.
   *
   * @return l'organisateur de l'événement
   */
  public User getOrganizer() {
    return organizer;
  }

  /**
   * Modifie l'utilisateur organisateur de l'événement.
   *
   * @param organizer nouvel organisateur de l'événement
   */
  public void setOrganizer(User organizer) {
    this.organizer = organizer;
  }

  /**
   * Retourne le club auquel appartient l'événement.
   *
   * @return le club associé à l'événement
   */
  public Club getClub() {
    return club;
  }

  /**
   * Modifie le club auquel appartient l'événement.
   *
   * @param club nouveau club associé à l'événement
   */
  public void setClub(Club club) {
    this.club = club;
  }

}