package fr.CDA.GHE.entity;

import fr.CDA.GHE.entity.enums.Category;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un club affilié au hub événementiel.
 */
@Entity
@Table(name = "clubs")
public class Club {

  /**
   * Identifiant unique du club.
   * Généré automatiquement par la base de données.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Nom du club.
   */
  @Column(nullable = false)
  private String name;

  /**
   * Catégorie du club.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Category category;

  /**
   * Adresse postale du club.
   */
  @Column(name = "postal_address", nullable = false)
  private String postalAddress;

  /**
   * Adresse email du club.
   */
  @Column(nullable = false)
  private String email;

  /**
   * Numéro de téléphone du club.
   */
  @Column(nullable = false)
  private String phone;

  /**
   * Date de fin de validité de l'affiliation du club.
   * <p>
   * Cette valeur est {@code null} tant que le club est affilié.
   * Elle est renseignée par le système lorsque l'affiliation du club prend fin.
   */
  @Column(name = "validity_end_date")
  private LocalDate validityEndDate;

  /**
   * Utilisateurs affiliés au club.
   * <p>
   * Club est le côté propriétaire de la relation ManyToMany avec User
   * et gère la table d'association {@code affiliation}.
   */
  @ManyToMany
  @JoinTable(
      name = "affiliation",
      joinColumns = @JoinColumn(name = "club_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<User> members = new HashSet<>();

  /**
   * Événements rattachés au club.
   * <p>
   * Event est le côté propriétaire de la relation grâce à son attribut {@code club}.
   */
  @OneToMany(mappedBy = "club")
  private Set<Event> events = new HashSet<>();

  /**
   * Constructeur vide requis par JPA.
   */
  public Club() {
  }

  /**
   * Initialise un club avec les informations saisissables lors de sa création.
   * <p>
   * La date de fin de validité n'est pas fournie lors de la création :
   * elle reste {@code null} tant que le club est affilié.
   *
   * @param name          nom du club
   * @param category      catégorie du club
   * @param postalAddress adresse postale du club
   * @param email         adresse email du club
   * @param phone         numéro de téléphone du club
   */
  public Club(
      String name,
      Category category,
      String postalAddress,
      String email,
      String phone
  ) {
    this.name = name;
    this.category = category;
    this.postalAddress = postalAddress;
    this.email = email;
    this.phone = phone;
  }

  /**
   * Retourne l'identifiant unique du club.
   *
   * @return l'identifiant du club
   */
  public Long getId() {
    return id;
  }

  /**
   * Retourne le nom du club.
   *
   * @return le nom du club
   */
  public String getName() {
    return name;
  }

  /**
   * Modifie le nom du club.
   *
   * @param name nouveau nom du club
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Retourne la catégorie du club.
   *
   * @return la catégorie du club
   */
  public Category getCategory() {
    return category;
  }

  /**
   * Modifie la catégorie du club.
   *
   * @param category nouvelle catégorie du club
   */
  public void setCategory(Category category) {
    this.category = category;
  }

  /**
   * Retourne l'adresse postale du club.
   *
   * @return l'adresse postale du club
   */
  public String getPostalAddress() {
    return postalAddress;
  }

  /**
   * Modifie l'adresse postale du club.
   *
   * @param postalAddress nouvelle adresse postale du club
   */
  public void setPostalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
  }

  /**
   * Retourne l'adresse email du club.
   *
   * @return l'adresse email du club
   */
  public String getEmail() {
    return email;
  }

  /**
   * Modifie l'adresse email du club.
   *
   * @param email nouvelle adresse email du club
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Retourne le numéro de téléphone du club.
   *
   * @return le numéro de téléphone du club
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Modifie le numéro de téléphone du club.
   *
   * @param phone nouveau numéro de téléphone du club
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * Retourne la date de fin de validité de l'affiliation du club.
   *
   * @return la date de fin de validité,
   * ou {@code null} tant que le club est affilié
   */
  public LocalDate getValidityEndDate() {
    return validityEndDate;
  }

  /**
   * Met fin à l'affiliation du club à la date indiquée.
   * <p>
   * Cette méthode permet de modifier la date de fin de validité
   * sans exposer de setter générique sur cet attribut métier.
   *
   * @param endDate date de fin de validité du club
   */
  public void endClubAffiliation(LocalDate endDate) {
    this.validityEndDate = endDate;
  }

  /**
   * Retourne les utilisateurs affiliés au club.
   *
   * @return les utilisateurs affiliés au club
   */
  public Set<User> getMembers() {
    return members;
  }

  /**
   * Ajoute un utilisateur aux membres affiliés du club
   * et synchronise le côté inverse de la relation.
   *
   * Si l'utilisateur est déjà présent dans la collection,
   * aucune modification supplémentaire n'est effectuée.
   *
   * @param member utilisateur à affilier au club
   */
  //À RÉACTIVER lorsque User.clubs sera présent sur dev.
  //public void addMember(User member) {
  //  if (members.add(member)) {
  //   member.getClubs().add(this);
  //  }
  //}

  /**
   * Retire un utilisateur des membres affiliés du club
   * et synchronise le côté inverse de la relation.
   *
   * Les conséquences métier éventuelles du retrait de la dernière
   * affiliation restent gérées par la couche service.
   *
   * @param member utilisateur à retirer du club
   * @return {@code true} si l'affiliation existait et a été retirée,
   *         {@code false} sinon
   */
  //À RÉACTIVER lorsque User.clubs sera présent sur dev.
  //public boolean removeMember(User member) {
  //  boolean removed = members.remove(member);

  //  if (removed) {
  //    member.getClubs().remove(this);
  //  }

  //  return removed;
  //}

  /**
   * Retourne les événements rattachés au club.
   *
   * @return les événements du club
   */
  public Set<Event> getEvents() {
    return events;
  }
}