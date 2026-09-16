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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    public AnonymizationRequest() {
    }

    public AnonymizationRequest(User user, RequestStatus status, LocalDateTime requestDate) {
        this.user = user;
        this.status = status;
        this.requestDate = requestDate;
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
}
