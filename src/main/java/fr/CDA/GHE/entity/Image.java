package fr.CDA.GHE.entity;

import jakarta.persistence.*;

/**
 * Entité représentant une image de la galerie d'un événement (CU23, SFG §2.26).
 * <p>
 * Le fichier lui-même est stocké sur le disque du serveur (pas en base) : {@code filePath}
 * n'est qu'un chemin relatif au répertoire de stockage configuré ({@code app.upload-dir}).
 */
@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Événement auquel cette image est associée.
     * <p>
     * Image est le côté propriétaire de cette relation grâce à la clé étrangère {@code event_id}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    public Image() {
    }

    public Image(Event event, String filePath, String contentType) {
        this.event = event;
        this.filePath = filePath;
        this.contentType = contentType;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
