package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.ImageContentDto;
import fr.CDA.GHE.dto.ImageDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.ForbiddenException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.ImageRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Tests de {@link ImageService} avec une vraie base H2 (profil "test", voir
 * {@code GheApplicationTests}) et une vraie écriture disque, dans le répertoire isolé
 * {@code target/test-uploads} (voir {@code application-test.properties}, nettoyé par
 * {@code mvn clean}) : vérifie que le fichier est réellement écrit/lu/supprimé, pas seulement
 * que le service appelle les bonnes méthodes sur un repository mocké.
 * <p>
 * {@code @Transactional} annule les écritures en base après chaque test (le fichier sur disque
 * n'est pas concerné par le rollback, mais reste dans le répertoire isolé de test).
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addImages_shouldStoreFileOnDiskAndPersistImage_whenCalledByOwner() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);

        MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", "fake-bytes".getBytes());

        List<ImageDto> created = imageService.addImages(event.getId(), List.of(file));

        assertThat(created).hasSize(1);
        assertThat(imageRepository.findByEvent_Id(event.getId())).hasSize(1);

        ImageContentDto content = imageService.getImageContent(event.getId(), created.get(0).id());
        assertThat(content.contentType()).isEqualTo("image/png");
        assertThat(new String(content.content())).isEqualTo("fake-bytes");
    }

    @Test
    void addImages_shouldThrow_whenNoFileProvided() {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);

        assertThatThrownBy(() -> imageService.addImages(event.getId(), List.of()))
                .isInstanceOf(FunctionalException.class);
    }

    @Test
    void addImages_shouldThrow_whenFileIsNotAnImage() {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);

        MockMultipartFile file = new MockMultipartFile("files", "doc.pdf", "application/pdf", "not-an-image".getBytes());

        assertThatThrownBy(() -> imageService.addImages(event.getId(), List.of(file)))
                .isInstanceOf(FunctionalException.class);
    }

    @Test
    void addImages_shouldThrow_whenCallerIsNotTheOwner() {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        User otherOrganizer = persistOrganizer("other@test.com");
        authenticateAs(otherOrganizer);

        MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", "fake-bytes".getBytes());

        assertThatThrownBy(() -> imageService.addImages(event.getId(), List.of(file)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void removeImage_shouldDeleteFileFromDiskAndRemoveImage() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);

        MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", "fake-bytes".getBytes());
        Long imageId = imageService.addImages(event.getId(), List.of(file)).get(0).id();

        imageService.removeImage(event.getId(), imageId);

        assertThat(imageRepository.findById(imageId)).isEmpty();
        assertThatThrownBy(() -> imageService.getImageContent(event.getId(), imageId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getImageContent_shouldThrow_whenImageDoesNotBelongToEvent() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        Event otherEvent = persistEvent(organizer);
        authenticateAs(organizer);

        MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", "fake-bytes".getBytes());
        Long imageId = imageService.addImages(event.getId(), List.of(file)).get(0).id();

        assertThatThrownBy(() -> imageService.getImageContent(otherEvent.getId(), imageId))
                .isInstanceOf(NotFoundException.class);
    }

    private User persistOrganizer(String email) {
        User user = new User();
        user.setLastName("Doe");
        user.setFirstName("John");
        user.setPostalAddress("1 rue de Test");
        user.setEmail(email);
        user.setPassword("hashed-password");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.ORGANIZER);
        return userRepository.save(user);
    }

    private Event persistEvent(User organizer) {
        Club club = clubRepository.save(new Club("Club de Test", Category.SPORT,
                "1 rue du Club", "club@test.com", "0500000000"));

        Event event = new Event("Titre", "Description", "Lieu",
                LocalDateTime.now().plusDays(1), null,
                BigDecimal.TEN, BigDecimal.TEN, 50, Category.SPORT, organizer, club);
        return eventRepository.save(event);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }
}
