package fr.CDA.GHE.dto;

/**
 * Contenu binaire d'une image, prêt à être renvoyé par le controller (CU23).
 *
 * @param content     octets bruts de l'image
 * @param contentType type MIME de l'image (ex. {@code image/png}), pour l'en-tête HTTP
 */
public record ImageContentDto(byte[] content, String contentType) {
}
