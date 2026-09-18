package fr.CDA.GHE.entity;

import jakarta.persistence.*;

/**
 * Entité représentant une image de la galerie d'un événement (CU23, SFG §2.26).
 * <p>
 * Le fichier est hébergé sur un service externe (imgbb) plutôt que sur le disque du serveur :
 * un déploiement sur un hébergeur cloud gratuit (ex. Render) a un système de fichiers éphémère
 * (perdu à chaque redéploiement/redémarrage), et le stockage en base (BLOB) est écarté pour des
 * raisons de sécurité et de performance.
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

    @Column(name = "url", nullable = false)
    private String url;

    /**
     * Lien de suppression retourné par imgbb à l'upload, utilisé pour retirer l'image de
     * l'hébergeur externe (voir {@link fr.CDA.GHE.service.ImgbbClient#delete}).
     */
    @Column(name = "delete_url", nullable = false)
    private String deleteUrl;

    public Image() {
    }

    public Image(Event event, String url, String deleteUrl) {
        this.event = event;
        this.url = url;
        this.deleteUrl = deleteUrl;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDeleteUrl() {
        return deleteUrl;
    }

    public void setDeleteUrl(String deleteUrl) {
        this.deleteUrl = deleteUrl;
    }
}
