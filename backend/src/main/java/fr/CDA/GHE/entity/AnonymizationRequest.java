package fr.CDA.GHE.entity;

import fr.CDA.GHE.entity.enums.RequestStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entité représentant une demande d'anonymisation d'un utilisateur (CU14, CU28, CU29).
 * <p>
 * Un utilisateur ne peut avoir qu'une seule demande d'anonymisation à la fois (contrainte
 * d'unicité sur la colonne {@code user_id}) : {@link RequestStatus#PENDING} tant qu'elle
 * n'a pas été traitée par un administrateur, {@link RequestStatus#VALIDATED} une fois
 * l'anonymisation effectuée.
 * </p>
 */
@Entity
@Table(name = "anonymization_requests")
public class AnonymizationRequest {

    /**
     * Identifiant unique de la demande d'anonymisation.
     * Généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Utilisateur à l'origine de la demande.
     * <p>
     * AnonymizationRequest est le côté propriétaire de cette relation grâce à la clé
     * étrangère {@code user_id}.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Statut actuel de la demande.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    /**
     * Date et heure de la demande.
     */
    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    /**
     * Constructeur vide requis par JPA.
     */
    public AnonymizationRequest() {
    }

    /**
     * Initialise une demande d'anonymisation avec toutes ses informations.
     *
     * @param user        utilisateur à l'origine de la demande
     * @param status      statut de la demande
     * @param requestDate date et heure de la demande
     */
    public AnonymizationRequest(User user, RequestStatus status, LocalDateTime requestDate) {
        this.user = user;
        this.status = status;
        this.requestDate = requestDate;
    }

    /**
     * Retourne l'identifiant unique de la demande.
     *
     * @return l'identifiant de la demande
     */
    public Long getId() {
        return id;
    }

    /**
     * Retourne l'utilisateur à l'origine de la demande.
     *
     * @return l'utilisateur concerné
     */
    public User getUser() {
        return user;
    }

    /**
     * Modifie l'utilisateur à l'origine de la demande.
     *
     * @param user nouvel utilisateur concerné
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Retourne le statut actuel de la demande.
     *
     * @return le statut de la demande
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Modifie le statut de la demande.
     *
     * @param status nouveau statut de la demande
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    /**
     * Retourne la date et l'heure de la demande.
     *
     * @return la date et l'heure de la demande
     */
    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    /**
     * Modifie la date et l'heure de la demande.
     *
     * @param requestDate nouvelle date et heure de la demande
     */
    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
}
