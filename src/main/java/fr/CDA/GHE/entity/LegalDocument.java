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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private DocumentType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LegalDocument() {
    }

    public LegalDocument(DocumentType type, String content, LocalDateTime updatedAt) {
        this.type = type;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
