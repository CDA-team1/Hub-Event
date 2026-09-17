package fr.CDA.GHE.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un commentaire publié sur un événement (CU12, SFG §2.15).
 */
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Événement sur lequel ce commentaire a été publié (règle métier n°2).
     * <p>
     * Comment est le côté propriétaire de cette relation grâce à la clé étrangère
     * {@code event_id}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Comment() {
    }

    public Comment(String content, User author, Event event, LocalDateTime createdAt) {
        this.content = content;
        this.author = author;
        this.event = event;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
