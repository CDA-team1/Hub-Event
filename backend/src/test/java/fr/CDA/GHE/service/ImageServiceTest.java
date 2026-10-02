package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.ImageDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Image;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de {@link ImageService} avec une vraie base H2 (profil "test", voir
 * {@code GheApplicationTests}) et de vrais repositories. {@link ImgbbClient} est mocké : c'est
 * une dépendance réellement externe (appel HTTP vers imgbb), comme {@code EmailService} dans
 * {@code UserServiceTest} — on ne veut pas d'appel réseau réel dans les tests.
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

    @MockitoBean
    private ImgbbClient imgbbClient;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addImages_shouldUploadToImgbbAndPersistImage_whenCalledByOwner() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);

        MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", "fake-bytes".getBytes());
        when(imgbbClient.upload(any()))
                .thenReturn(new ImgbbClient.UploadedImage("https://i.ibb.co/abc/photo.png", "https://ibb.co/delete/abc"));

        List<ImageDto> created = imageService.addImages(event.getId(), List.of(file));

        assertThat(created).hasSize(1);
        assertThat(created.get(0).url()).isEqualTo("https://i.ibb.co/abc/photo.png");

        Image persisted = imageRepository.findByEvent_Id(event.getId()).get(0);
        assertThat(persisted.getUrl()).isEqualTo("https://i.ibb.co/abc/photo.png");
        assertThat(persisted.getDeleteUrl()).isEqualTo("https://ibb.co/delete/abc");
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
    void removeImage_shouldDeleteFromImgbbAndRemoveImage() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);

        MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", "fake-bytes".getBytes());
        when(imgbbClient.upload(any()))
                .thenReturn(new ImgbbClient.UploadedImage("https://i.ibb.co/abc/photo.png", "https://ibb.co/delete/abc"));
        Long imageId = imageService.addImages(event.getId(), List.of(file)).get(0).id();

        imageService.removeImage(event.getId(), imageId);

        assertThat(imageRepository.findById(imageId)).isEmpty();
        verify(imgbbClient).delete("https://ibb.co/delete/abc");
    }

    @Test
    void removeImage_shouldThrow_whenImageDoesNotBelongToEvent() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        Event otherEvent = persistEvent(organizer);
        authenticateAs(organizer);

        MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", "fake-bytes".getBytes());
        when(imgbbClient.upload(any()))
                .thenReturn(new ImgbbClient.UploadedImage("https://i.ibb.co/abc/photo.png", "https://ibb.co/delete/abc"));
        Long imageId = imageService.addImages(event.getId(), List.of(file)).get(0).id();

        assertThatThrownBy(() -> imageService.removeImage(otherEvent.getId(), imageId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addImages_shouldMakeOnlyTheFirstImagePreview_whenEventHasNoImage() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);
        when(imgbbClient.upload(any())).thenReturn(
                new ImgbbClient.UploadedImage("https://i.ibb.co/1.png", "https://ibb.co/delete/1"),
                new ImgbbClient.UploadedImage("https://i.ibb.co/2.png", "https://ibb.co/delete/2"));

        List<ImageDto> created = imageService.addImages(event.getId(), List.of(photo(), photo()));

        assertThat(created).extracting(ImageDto::isPreview).containsExactly(true, false);
    }

    @Test
    void addImages_shouldKeepExistingPreview_whenEventAlreadyHasOne() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);
        when(imgbbClient.upload(any())).thenReturn(
                new ImgbbClient.UploadedImage("https://i.ibb.co/1.png", "https://ibb.co/delete/1"),
                new ImgbbClient.UploadedImage("https://i.ibb.co/2.png", "https://ibb.co/delete/2"));

        imageService.addImages(event.getId(), List.of(photo()));
        List<ImageDto> second = imageService.addImages(event.getId(), List.of(photo()));

        assertThat(second.get(0).isPreview()).isFalse();
        assertThat(imageRepository.findByEvent_IdInAndIsPreviewTrue(List.of(event.getId()))).hasSize(1);
    }

    @Test
    void changePreview_shouldMakeChosenImageTheOnlyPreview() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);
        when(imgbbClient.upload(any())).thenReturn(
                new ImgbbClient.UploadedImage("https://i.ibb.co/1.png", "https://ibb.co/delete/1"),
                new ImgbbClient.UploadedImage("https://i.ibb.co/2.png", "https://ibb.co/delete/2"));
        List<ImageDto> created = imageService.addImages(event.getId(), List.of(photo(), photo()));
        Long secondId = created.get(1).id();

        imageService.changePreview(event.getId(), secondId);

        List<Image> previews = imageRepository.findByEvent_IdInAndIsPreviewTrue(List.of(event.getId()));
        assertThat(previews).extracting(Image::getId).containsExactly(secondId);
        assertThat(imageService.findCoverImageUrlsByEventIds(List.of(event.getId())))
                .containsEntry(event.getId(), "https://i.ibb.co/2.png");
    }

    @Test
    void changePreview_shouldThrow_whenCallerIsNotTheOwner() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);
        when(imgbbClient.upload(any()))
                .thenReturn(new ImgbbClient.UploadedImage("https://i.ibb.co/1.png", "https://ibb.co/delete/1"));
        Long imageId = imageService.addImages(event.getId(), List.of(photo())).get(0).id();
        authenticateAs(persistOrganizer("other@test.com"));

        assertThatThrownBy(() -> imageService.changePreview(event.getId(), imageId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void removeImage_shouldPromoteOldestRemainingImage_whenPreviewIsRemoved() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);
        when(imgbbClient.upload(any())).thenReturn(
                new ImgbbClient.UploadedImage("https://i.ibb.co/1.png", "https://ibb.co/delete/1"),
                new ImgbbClient.UploadedImage("https://i.ibb.co/2.png", "https://ibb.co/delete/2"));
        List<ImageDto> created = imageService.addImages(event.getId(), List.of(photo(), photo()));

        imageService.removeImage(event.getId(), created.get(0).id());

        assertThat(imageRepository.findById(created.get(1).id()).orElseThrow().isPreview()).isTrue();
    }

    @Test
    void findGalleryForEvent_shouldReturnPreviewFirst() throws FunctionalException {
        User organizer = persistOrganizer("organizer@test.com");
        Event event = persistEvent(organizer);
        authenticateAs(organizer);
        when(imgbbClient.upload(any())).thenReturn(
                new ImgbbClient.UploadedImage("https://i.ibb.co/1.png", "https://ibb.co/delete/1"),
                new ImgbbClient.UploadedImage("https://i.ibb.co/2.png", "https://ibb.co/delete/2"),
                new ImgbbClient.UploadedImage("https://i.ibb.co/3.png", "https://ibb.co/delete/3"));
        List<ImageDto> created = imageService.addImages(event.getId(), List.of(photo(), photo(), photo()));
        Long thirdId = created.get(2).id();
        imageService.changePreview(event.getId(), thirdId);

        List<ImageDto> gallery = imageService.findGalleryForEvent(event.getId());

        assertThat(gallery).extracting(ImageDto::id)
                .containsExactly(thirdId, created.get(0).id(), created.get(1).id());
        assertThat(gallery.get(0).isPreview()).isTrue();
    }

    private MockMultipartFile photo() {
        return new MockMultipartFile("files", "photo.png", "image/png", "fake-bytes".getBytes());
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
