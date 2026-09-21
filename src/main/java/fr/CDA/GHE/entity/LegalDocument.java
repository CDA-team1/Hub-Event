package fr.CDA.GHE.entity;

import fr.CDA.GHE.entity.enums.DocumentType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un document légal de la plateforme (CGU ou politique RGPD) —
 * CU31, CU32, SFG §2.34/2.35.
 * <p>
 * Un seul document par type (contrainte d'unicité sur la colonne {@code type}).
 */
@Entity
@Table(name = "legal_documents")
public class LegalDocument {

    /**
     * Identifiant unique du document légal.
     * Généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type du document légal.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private DocumentType type;

    /**
     * Contenu textuel du document.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Date et heure de la dernière modification du document.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructeur vide requis par JPA.
     */
    public LegalDocument() {
    }

    /**
     * Initialise un document légal avec toutes ses informations.
     *
     * @param type      type du document
     * @param content   contenu textuel du document
     * @param updatedAt date et heure de la dernière modification
     */
    public LegalDocument(DocumentType type, String content, LocalDateTime updatedAt) {
        this.type = type;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    /**
     * Retourne l'identifiant unique du document.
     *
     * @return l'identifiant du document
     */
    public Long getId() {
        return id;
    }

    /**
     * Retourne le type du document.
     *
     * @return le type du document
     */
    public DocumentType getType() {
        return type;
    }

    /**
     * Modifie le type du document.
     *
     * @param type nouveau type du document
     */
    public void setType(DocumentType type) {
        this.type = type;
    }

    /**
     * Retourne le contenu textuel du document.
     *
     * @return le contenu du document
     */
    public String getContent() {
        return content;
    }

    /**
     * Modifie le contenu textuel du document.
     *
     * @param content nouveau contenu du document
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Retourne la date et l'heure de la dernière modification.
     *
     * @return la date de dernière modification
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Modifie la date et l'heure de la dernière modification.
     *
     * @param updatedAt nouvelle date de dernière modification
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
