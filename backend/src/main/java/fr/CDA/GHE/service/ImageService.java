package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.ImageDto;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Image;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.ForbiddenException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.ImageMapper;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.ImageRepository;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.util.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;
import java.util.ArrayList;

/**
 * Gère la galerie photos d'un événement (CU23, SFG §2.26).
 * <p>
 * Les fichiers sont hébergés sur un service externe (imgbb, voir {@link ImgbbClient}) ; seuls le
 * lien direct et le lien de suppression sont persistés en base ({@link Image}).
 */
@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ImageMapper imageMapper;
    private final ImgbbClient imgbbClient;
    private final int maxFilesPerRequest;
    private final DataSize maxFileSize;

    /**
     * @param maxFilesPerRequest nombre maximal de fichiers acceptés dans un même envoi
     *                           ({@code app.images.max-files-per-request}) ; ne limite pas le total
     *                           d'images d'un événement, seulement la taille d'un envoi
     * @param maxFileSize        taille maximale d'un fichier : la même valeur que la limite
     *                           {@code spring.servlet.multipart.max-file-size}, pour n'avoir qu'un
     *                           seul réglage
     */
    public ImageService(ImageRepository imageRepository, EventRepository eventRepository,
                         UserRepository userRepository, ImageMapper imageMapper, ImgbbClient imgbbClient,
                         @Value("${app.images.max-files-per-request:20}") int maxFilesPerRequest,
                         @Value("${spring.servlet.multipart.max-file-size:5MB}") String maxFileSize) {
        this.maxFilesPerRequest = maxFilesPerRequest;
        this.maxFileSize = DataSize.parse(maxFileSize);
        this.imageRepository = imageRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.imageMapper = imageMapper;
        this.imgbbClient = imgbbClient;
    }

    /**
     * Ajoute une ou plusieurs images à la galerie d'un événement (CU23, règles n°1 à 3).
     * <p>
     * Réservé à l'organisateur propriétaire de l'événement. L'ajout d'images reste possible
     * quel que soit le statut de l'événement (y compris FINISHED) : contrairement aux autres
     * champs, la galerie ne passe pas par {@code EventService.updateEvent}, qui bloque
     * justement toute modification d'un événement FINISHED en dehors de ses images.
     *
     * @param eventId identifiant de l'événement concerné
     * @param files   fichiers image envoyés
     * @return les images créées
     * @throws FunctionalException si aucun fichier n'est fourni, si l'envoi dépasse le nombre de
     *                             fichiers autorisé, ou si un fichier est trop volumineux ou n'est
     *                             pas une image JPEG, PNG, WebP ou GIF
     */
    @Transactional
    public List<ImageDto> addImages(Long eventId, List<MultipartFile> files) throws FunctionalException {
        Event event = findEventOrThrow(eventId);
        checkOwnership(event);

        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            throw new FunctionalException("Veuillez sélectionner au moins une image.");
        }

        List<MultipartFile> nonEmptyFiles = files.stream().filter(file -> !file.isEmpty()).toList();

        if (nonEmptyFiles.size() > maxFilesPerRequest) {
            throw new FunctionalException(
                    "Vous pouvez envoyer " + maxFilesPerRequest + " images au maximum à la fois.");
        }

        for (MultipartFile file : nonEmptyFiles) {
            validateImage(file);
        }

        boolean eventHasPreview = imageRepository.existsByEvent_IdAndIsPreviewTrue(eventId);

        List<Image> created = new ArrayList<>();
        for (MultipartFile file : nonEmptyFiles) {
            boolean makePreview = !eventHasPreview && created.isEmpty();
            created.add(uploadAndPersist(event, file, makePreview));
        }

        log.info("AJOUT images : eventId={} count={}", eventId, created.size());

        return imageMapper.toDtoList(created);
    }

    /**
     * Retire une image de la galerie d'un événement (CU23, règle n°4).
     * <p>
     * Réservé à l'organisateur propriétaire de l'événement.
     *
     * @param eventId identifiant de l'événement concerné
     * @param imageId identifiant de l'image à retirer
     * @throws NotFoundException si l'événement ou l'image n'existe pas, ou si l'image
     *                           n'appartient pas à cet événement
     */
    @Transactional
    public void removeImage(Long eventId, Long imageId) {
        Event event = findEventOrThrow(eventId);
        checkOwnership(event);

        Image image = findImageOrThrow(eventId, imageId);
        boolean wasPreview = image.isPreview();

        imageRepository.delete(image);
        imgbbClient.delete(image.getDeleteUrl());

        if (wasPreview) {
            imageRepository.findByEvent_Id(eventId).stream()
                    .filter(remaining -> !remaining.getId().equals(imageId))
                    .min(Comparator.comparing(Image::getId))
                    .ifPresent(next -> next.setPreview(true));
        }

        log.info("SUPPRESSION image : eventId={} imageId={}", eventId, imageId);
    }

    /**
     * Contrôle un fichier avant son envoi à l'hébergeur : taille, puis type réel.
     * <p>
     * Le type réel est lu dans les premiers octets du fichier (sa « signature »). L'en-tête
     * {@code Content-Type} et l'extension du nom sont fournis par le client et ne prouvent rien :
     * un fichier HTML ou SVG peut s'annoncer comme {@code image/png}.
     * </p>
     */
    private void validateImage(MultipartFile file) throws FunctionalException {
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new FunctionalException(
                    "Chaque image doit faire " + maxFileSize.toMegabytes() + " Mo maximum.");
        }

        if (!hasSupportedImageSignature(file)) {
            throw new FunctionalException("Chaque fichier doit être une image JPEG, PNG, WebP ou GIF.");
        }
    }

    private static boolean hasSupportedImageSignature(MultipartFile file) throws FunctionalException {
        byte[] header;
        try (InputStream input = file.getInputStream()) {
            header = input.readNBytes(12);
        } catch (IOException exception) {
            throw new FunctionalException("Le fichier envoyé est illisible.");
        }

        return isJpeg(header) || isPng(header) || isGif(header) || isWebp(header);
    }

    private static boolean isJpeg(byte[] header) {
        return startsWith(header, 0, 0xFF, 0xD8, 0xFF);
    }

    private static boolean isPng(byte[] header) {
        return startsWith(header, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
    }

    private static boolean isGif(byte[] header) {
        return startsWith(header, 0, 'G', 'I', 'F', '8', '7', 'a')
                || startsWith(header, 0, 'G', 'I', 'F', '8', '9', 'a');
    }

    /** WebP est un conteneur RIFF : « RIFF », 4 octets de taille, puis « WEBP ». */
    private static boolean isWebp(byte[] header) {
        return startsWith(header, 0, 'R', 'I', 'F', 'F') && startsWith(header, 8, 'W', 'E', 'B', 'P');
    }

    private static boolean startsWith(byte[] data, int offset, int... expected) {
        if (data.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((data[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private Image uploadAndPersist(Event event, MultipartFile file, boolean isPreview) {
        ImgbbClient.UploadedImage uploaded = imgbbClient.upload(file);
        Image image = new Image(event, uploaded.url(), uploaded.deleteUrl());
        image.setPreview(isPreview);
        return imageRepository.save(image);
    }

        /**
         * Désigne une image comme preview de son événement, l'ancienne preview est désactivée.
         * <p>
         * Réservé à l'organisateur propriétaire de l'événement.
         *
         * @param eventId identifiant de l'événement concerné
         * @param imageId identifiant de l'image à définir comme preview
         * @throws NotFoundException si l'événement ou l'image n'existe pas, ou si l'image
         *                           n'appartient pas à cet événement
         */
        @Transactional
        public void changePreview(Long eventId, Long imageId) {
            Event event = findEventOrThrow(eventId);
            checkOwnership(event);

            Image newPreview = findImageOrThrow(eventId, imageId);

            imageRepository.findByEvent_Id(eventId).forEach(image -> image.setPreview(false));
            newPreview.setPreview(true);

            log.info("CHANGEMENT preview : eventId={} imageId={}", eventId, imageId);
        }

    /**
     * Supprime toutes les images associées à un événement, en base et sur l'hébergeur externe
     * (imgbb). Appelé avant la suppression définitive d'un événement (contrainte FK sur
     * {@code images.event_id}).
     *
     * @param eventId identifiant de l'événement concerné
     */
    @Transactional
    public void removeAllImagesForEvent(Long eventId) {
        List<Image> images = imageRepository.findByEvent_Id(eventId);

        imageRepository.deleteAll(images);

        for (Image image : images) {
            imgbbClient.delete(image.getDeleteUrl());
        }
    }

        /**
         * Retourne l'image de prévisualisation (preview) de plusieurs événements à la fois, pour
         * l'affichage en liste (cartes).
         * <p>
         * Les événements sans image n'apparaissent pas dans la map retournée.
         *
         * @param eventIds identifiants des événements concernés
         * @return une map associant chaque identifiant d'événement à l'URL de sa preview
         */
        public Map<Long, String> findCoverImageUrlsByEventIds(List<Long> eventIds) {
            return imageRepository.findByEvent_IdInAndIsPreviewTrue(eventIds).stream()
                    .collect(Collectors.toMap(
                            image -> image.getEvent().getId(),
                            Image::getUrl,
                            (first, second) -> first
                    ));
        }

        /**
         * Retourne la galerie complète d'un événement, pour l'affichage sur sa page de détail :
         * la preview en premier, puis les autres images par ordre d'ajout.
         *
         * @param eventId identifiant de l'événement concerné
         * @return la liste des images de la galerie, sous forme de DTO
         */
        public List<ImageDto> findGalleryForEvent(Long eventId) {
            List<Image> images = imageRepository.findByEvent_Id(eventId).stream()
                    .sorted(Comparator.comparing(Image::isPreview).reversed()
                            .thenComparing(Image::getId))
                    .toList();
            return imageMapper.toDtoList(images);
        }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));
    }

    private Image findImageOrThrow(Long eventId, Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image introuvable"));

        if (!image.getEvent().getId().equals(eventId)) {
            throw new NotFoundException("Image introuvable");
        }

        return image;
    }

    private void checkOwnership(Event event) {
        User organizer = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (organizer.getRole() != Role.ORGANIZER || !event.getOrganizer().getId().equals(organizer.getId())) {
            throw new ForbiddenException("Vous n'êtes pas autorisé à modifier la galerie de cet événement.");
        }
    }
}
