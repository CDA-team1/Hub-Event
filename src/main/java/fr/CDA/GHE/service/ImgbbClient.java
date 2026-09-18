package fr.CDA.GHE.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Envoie les images de la galerie des événements vers imgbb (hébergement externe), plutôt que de
 * les stocker sur le disque du serveur ou en base.
 * <p>
 * Nécessaire pour un déploiement sur un hébergeur cloud gratuit (ex. Render), dont le système de
 * fichiers est éphémère (perdu à chaque redéploiement/redémarrage) — voir échange avec le
 * formateur, 2026-09-18. Le stockage en base (BLOB) est écarté également (déconseillé en usage
 * relationnel : gonfle la base, ralentit les sauvegardes, et certains moteurs tentent
 * d'interpréter le contenu).
 */
@Component
public class ImgbbClient {

    private static final Logger log = LoggerFactory.getLogger(ImgbbClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public ImgbbClient(@Value("${app.imgbb.api-key}") String apiKey) {
        this.restClient = RestClient.create("https://api.imgbb.com");
        this.apiKey = apiKey;
    }

    /**
     * Envoie un fichier image à imgbb.
     *
     * @param file fichier image à héberger
     * @return le lien direct de l'image et son lien de suppression
     * @throws IllegalStateException si l'envoi échoue (imgbb injoignable, réponse invalide, etc.)
     */
    public UploadedImage upload(MultipartFile file) {
        try {
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", resource);

            ImgbbResponse response = restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/1/upload").queryParam("key", apiKey).build())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(ImgbbResponse.class);

            if (response == null || !response.success() || response.data() == null) {
                throw new IllegalStateException("Réponse imgbb invalide");
            }

            return new UploadedImage(response.data().url(), response.data().deleteUrl());
        } catch (IOException | RestClientException e) {
            throw new IllegalStateException("Échec de l'envoi de l'image vers l'hébergeur externe", e);
        }
    }

    /**
     * Supprime une image précédemment envoyée à imgbb, en meilleur effort (pas d'API de
     * suppression officielle chez imgbb : on rejoue simplement le lien de suppression fourni à
     * l'upload). Un échec ici ne doit jamais bloquer la suppression côté application.
     *
     * @param deleteUrl lien de suppression fourni par imgbb à l'upload
     */
    public void delete(String deleteUrl) {
        try {
            restClient.get().uri(deleteUrl).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Impossible de supprimer l'image sur imgbb ({})", deleteUrl, e);
        }
    }

    public record UploadedImage(String url, String deleteUrl) {
    }

    private record ImgbbResponse(boolean success, ImgbbData data) {
    }

    private record ImgbbData(String url, @JsonProperty("delete_url") String deleteUrl) {
    }
}
