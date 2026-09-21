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

    /**
     * Identifiant unique de l'image.
     * Généré automatiquement par la base de données.
     */
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

    /**
     * URL publique de l'image, hébergée sur imgbb.
     */
    @Column(name = "url", nullable = false)
    private String url;

    /**
     * Lien de suppression retourné par imgbb à l'upload, utilisé pour retirer l'image de
     * l'hébergeur externe (voir {@link fr.CDA.GHE.service.ImgbbClient#delete}).
     */
    @Column(name = "delete_url", nullable = false)
    private String deleteUrl;

    /**
     * Constructeur vide requis par JPA.
     */
    public Image() {
    }

    /**
     * Initialise une image avec toutes ses informations.
     *
     * @param event     événement auquel l'image est associée
     * @param url       URL publique de l'image
     * @param deleteUrl lien de suppression retourné par imgbb
     */
    public Image(Event event, String url, String deleteUrl) {
        this.event = event;
        this.url = url;
        this.deleteUrl = deleteUrl;
    }

    /**
     * Retourne l'identifiant unique de l'image.
     *
     * @return l'identifiant de l'image
     */
    public Long getId() {
        return id;
    }

    /**
     * Retourne l'événement auquel cette image est associée.
     *
     * @return l'événement associé
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Modifie l'événement auquel cette image est associée.
     *
     * @param event nouvel événement associé
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Retourne l'URL publique de l'image.
     *
     * @return l'URL de l'image
     */
    public String getUrl() {
        return url;
    }

    /**
     * Modifie l'URL publique de l'image.
     *
     * @param url nouvelle URL de l'image
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Retourne le lien de suppression de l'image sur imgbb.
     *
     * @return le lien de suppression
     */
    public String getDeleteUrl() {
        return deleteUrl;
    }

    /**
     * Modifie le lien de suppression de l'image sur imgbb.
     *
     * @param deleteUrl nouveau lien de suppression
     */
    public void setDeleteUrl(String deleteUrl) {
        this.deleteUrl = deleteUrl;
    }
}
