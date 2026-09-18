package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.ImageContentDto;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Gère la galerie photos d'un événement (CU23, SFG §2.26).
 * <p>
 * Les fichiers sont stockés sur le disque du serveur, sous {@code app.upload-dir} ; seul le
 * chemin relatif est persisté en base ({@link Image#getFilePath()}).
 */
@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ImageMapper imageMapper;
    private final Path uploadDir;

    public ImageService(ImageRepository imageRepository, EventRepository eventRepository,
                         UserRepository userRepository, ImageMapper imageMapper,
                         @Value("${app.upload-dir}") String uploadDir) {
        this.imageRepository = imageRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.imageMapper = imageMapper;
        this.uploadDir = Path.of(uploadDir);
    }

    /**
     * Ajoute une ou plusieurs images à la galerie d'un événement (CU23, règles n°1 à 3).
     * <p>
     * Réservé à l'organisateur propriétaire de l'événement. L'ajout d'images reste possible
     * quel que soit le statut de l'événement (y compris FINISHED — voir le TODO EVT-05 sur
     * {@code EventService.updateEvent}, qui prévoit explicitement cette exception).
     *
     * @param eventId identifiant de l'événement concerné
     * @param files   fichiers image envoyés
     * @return les images créées
     * @throws FunctionalException si aucun fichier n'est fourni, ou si un fichier n'est pas une image
     */
    @Transactional
    public List<ImageDto> addImages(Long eventId, List<MultipartFile> files) throws FunctionalException {
        Event event = findEventOrThrow(eventId);
        checkOwnership(event);

        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            throw new FunctionalException("Veuillez sélectionner au moins une image.");
        }

        List<MultipartFile> nonEmptyFiles = files.stream().filter(file -> !file.isEmpty()).toList();
        for (MultipartFile file : nonEmptyFiles) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new FunctionalException("Chaque fichier doit être une image.");
            }
        }

        List<Image> created = nonEmptyFiles.stream()
                .map(file -> storeAndPersist(event, file))
                .toList();

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

        imageRepository.delete(image);
        deleteFileQuietly(image.getFilePath());

        log.info("SUPPRESSION image : eventId={} imageId={}", eventId, imageId);
    }

  /**
   * Supprime toutes les images associées à un événement,
   * en base de données et sur le disque.
   *
   * @param eventId identifiant de l'événement concerné
   */
  @Transactional
  public void removeAllImagesForEvent(Long eventId) {

    List<Image> images = imageRepository.findByEvent_Id(eventId);

    imageRepository.deleteAll(images);

    for (Image image : images) {
      deleteFileQuietly(image.getFilePath());
    }
  }

    /**
     * Retourne le contenu binaire d'une image (consultation publique, comme le détail de
     * l'événement).
     *
     * @param eventId identifiant de l'événement concerné
     * @param imageId identifiant de l'image demandée
     * @return le contenu de l'image et son type MIME
     * @throws NotFoundException si l'événement ou l'image n'existe pas, ou si l'image
     *                           n'appartient pas à cet événement
     */
    @Transactional(readOnly = true)
    public ImageContentDto getImageContent(Long eventId, Long imageId) {
        Image image = findImageOrThrow(eventId, imageId);

        try {
            byte[] content = Files.readAllBytes(uploadDir.resolve(image.getFilePath()));
            return new ImageContentDto(content, image.getContentType());
        } catch (IOException e) {
            throw new NotFoundException("Image introuvable");
        }
    }

    private Image storeAndPersist(Event event, MultipartFile file) {
        String fileName = UUID.randomUUID() + extractExtension(file.getOriginalFilename());
        String relativePath = "events/" + event.getId() + "/" + fileName;

        try {
            Path targetPath = uploadDir.resolve(relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
        } catch (IOException e) {
            // Ne devrait arriver qu'en cas de disque plein/permissions serveur ; pas une erreur
            // de saisie utilisateur, donc pas une FunctionalException.
            throw new IllegalStateException("Échec de l'enregistrement de l'image", e);
        }

        return imageRepository.save(new Image(event, relativePath, file.getContentType()));
    }

    private void deleteFileQuietly(String relativePath) {
        try {
            Files.deleteIfExists(uploadDir.resolve(relativePath));
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier {} du disque", relativePath, e);
        }
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

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
