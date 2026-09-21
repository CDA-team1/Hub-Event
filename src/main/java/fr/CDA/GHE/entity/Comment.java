package fr.CDA.GHE.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un commentaire publié sur un événement (CU12, SFG §2.15).
 */
@Entity
@Table(name = "comments")
public class Comment {

    /**
     * Identifiant unique du commentaire.
     * Généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Contenu textuel du commentaire.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Utilisateur ayant publié le commentaire.
     * <p>
     * Comment est le côté propriétaire de cette relation grâce à la clé étrangère
     * {@code author_id}. Conservé même si l'auteur est anonymisé par la suite (règle métier
     * n°5) : c'est l'affichage (mention « SUPPRIMÉ »), pas la donnée, qui change alors.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Événement sur lequel ce commentaire a été publié (règle métier n°2).
     * <p>
     * Comment est le côté propriétaire de cette relation grâce à la clé étrangère
     * {@code event_id}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /**
     * Date et heure de publication du commentaire.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Constructeur vide requis par JPA.
     */
    public Comment() {
    }

    /**
     * Initialise un commentaire avec toutes ses informations.
     *
     * @param content   contenu textuel du commentaire
     * @param user      utilisateur ayant publié le commentaire
     * @param event     événement sur lequel le commentaire est publié
     * @param createdAt date et heure de publication
     */
    public Comment(String content, User user, Event event, LocalDateTime createdAt) {
        this.content = content;
        this.user = user;
        this.event = event;
        this.createdAt = createdAt;
    }

    /**
     * Retourne l'identifiant unique du commentaire.
     *
     * @return l'identifiant du commentaire
     */
    public Long getId() {
        return id;
    }

    /**
     * Retourne le contenu textuel du commentaire.
     *
     * @return le contenu du commentaire
     */
    public String getContent() {
        return content;
    }

    /**
     * Modifie le contenu textuel du commentaire.
     *
     * @param content nouveau contenu du commentaire
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Retourne l'utilisateur ayant publié le commentaire.
     *
     * @return l'utilisateur auteur du commentaire
     */
    public User getUser() {
        return user;
    }

    /**
     * Modifie l'utilisateur ayant publié le commentaire.
     *
     * @param user nouvel utilisateur auteur du commentaire
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Retourne l'événement sur lequel le commentaire a été publié.
     *
     * @return l'événement concerné
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Modifie l'événement sur lequel le commentaire a été publié.
     *
     * @param event nouvel événement concerné
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Retourne la date et l'heure de publication du commentaire.
     *
     * @return la date de publication
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Modifie la date et l'heure de publication du commentaire.
     *
     * @param createdAt nouvelle date de publication
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
