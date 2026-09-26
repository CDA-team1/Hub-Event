package fr.CDA.GHE.dto;

/**
 * Données saisies par un administrateur pour enregistrer ou mettre à jour un document légal
 * (CU31, CU32). Le type est porté par l'URL ({@code PUT /admin/documents/{type}}), pas par
 * le corps de la requête.
 *
 * @param content contenu du document
 */
public record LegalDocumentRequest(String content) {
}
