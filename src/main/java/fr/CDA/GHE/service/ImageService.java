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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    public ImageService(ImageRepository imageRepository, EventRepository eventRepository,
                         UserRepository userRepository, ImageMapper imageMapper, ImgbbClient imgbbClient) {
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
                .map(file -> uploadAndPersist(event, file))
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
        imgbbClient.delete(image.getDeleteUrl());

        log.info("SUPPRESSION image : eventId={} imageId={}", eventId, imageId);
    }

    private Image uploadAndPersist(Event event, MultipartFile file) {
        ImgbbClient.UploadedImage uploaded = imgbbClient.upload(file);
        return imageRepository.save(new Image(event, uploaded.url(), uploaded.deleteUrl()));
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
