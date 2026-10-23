package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.CreateCommentRequest;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Comment;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.CommentRepository;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
 * Tests de {@link CommentService} avec une vraie base H2 (profil "test", voir
 * {@code GheApplicationTests}) : vérifie le comportement réel des requêtes dérivées (tri
 * chronologique, existence de l'événement), du formatage de l'auteur, et de la règle de
 * visibilité (COM-01) partagée avec {@link EventService#isEventAccessible}, plutôt qu'un
 * repository mocké.
 * <p>
 * {@code @Transactional} annule les écritures après chaque test.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    private static final String CONTENT = "Super évènement, merci à l'organisation !";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createComment_shouldPersistComment_whenAuthorIsActive() throws FunctionalException {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        Event event = persistEvent(author);
        authenticateAs(author);

        CommentDto dto = commentService.createComment(new CreateCommentRequest(event.getId(), CONTENT));

        assertThat(dto.content()).isEqualTo(CONTENT);
        assertThat(dto.eventId()).isEqualTo(event.getId());
        assertThat(dto.authorDisplayName()).isEqualTo("John D.");
        assertThat(commentRepository.findByEvent_IdOrderByCreatedAtAsc(event.getId())).hasSize(1);
    }

    @Test
    void createComment_shouldThrow_whenContentIsBlank() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        Event event = persistEvent(author);
        authenticateAs(author);

        assertThatThrownBy(() -> commentService.createComment(new CreateCommentRequest(event.getId(), "   ")))
                .isInstanceOf(FunctionalException.class);
    }

    @Test
    void createComment_shouldThrow_whenAuthorIsNotActive() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        Event event = persistEvent(author);
        author.setStatus(AccountStatus.INACTIVE);
        userRepository.save(author);
        authenticateAs(author);

        assertThatThrownBy(() -> commentService.createComment(new CreateCommentRequest(event.getId(), CONTENT)))
                .isInstanceOf(FunctionalException.class);
    }

    @Test
    void createComment_shouldThrow_whenEventDoesNotExist() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        authenticateAs(author);

        assertThatThrownBy(() -> commentService.createComment(new CreateCommentRequest(999L, CONTENT)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void extractByEvent_shouldReturnCommentsOrderedByCreationDate() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        Event event = persistEvent(author);
        authenticateAs(author);

        commentRepository.save(new Comment("Deuxième message", author, event,
                LocalDateTime.of(2026, 1, 2, 10, 0)));
        commentRepository.save(new Comment("Premier message", author, event,
                LocalDateTime.of(2026, 1, 1, 10, 0)));

        List<CommentDto> comments = commentService.extractByEvent(event.getId());

        assertThat(comments).extracting(CommentDto::content)
                .containsExactly("Premier message", "Deuxième message");
    }

    @Test
    void extractByEvent_shouldDisplaySuppressed_whenAuthorIsAnonymized() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        Event event = persistEvent(author);
        commentRepository.save(new Comment(CONTENT, author, event, LocalDateTime.now()));
        authenticateAs(author);

        author.setStatus(AccountStatus.ANONYMIZED);
        userRepository.save(author);

        List<CommentDto> comments = commentService.extractByEvent(event.getId());

        assertThat(comments).extracting(CommentDto::authorDisplayName).containsExactly("SUPPRIMÉ");
    }

    @Test
    void extractByEvent_shouldThrow_whenEventDoesNotExist() {
        assertThatThrownBy(() -> commentService.extractByEvent(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void extractByEvent_shouldReturnComments_whenEventIsPublishedAndUserIsAnonymous() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        Event event = persistPublishedEvent(author);
        commentRepository.save(new Comment(CONTENT, author, event, LocalDateTime.now()));

        List<CommentDto> comments = commentService.extractByEvent(event.getId());

        assertThat(comments).hasSize(1);
    }

    @Test
    void extractByEvent_shouldReturnComments_whenEventIsPublishedAndUserIsAuthenticated() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        User reader = persistActiveUser("reader@test.com", "Reader", "Rita");
        Event event = persistPublishedEvent(author);
        commentRepository.save(new Comment(CONTENT, author, event, LocalDateTime.now()));
        authenticateAs(reader);

        List<CommentDto> comments = commentService.extractByEvent(event.getId());

        assertThat(comments).hasSize(1);
    }

    @Test
    void extractByEvent_shouldThrow_whenEventIsDraftAndUserIsAnonymous() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        Event event = persistEvent(author);
        commentRepository.save(new Comment(CONTENT, author, event, LocalDateTime.now()));

        assertThatThrownBy(() -> commentService.extractByEvent(event.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void extractByEvent_shouldThrow_whenEventIsDraftAndUserIsNotOwner() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        User other = persistActiveUser("other@test.com", "Martin", "Paul");
        Event event = persistEvent(author);
        commentRepository.save(new Comment(CONTENT, author, event, LocalDateTime.now()));
        authenticateAs(other);

        assertThatThrownBy(() -> commentService.extractByEvent(event.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void extractByEvent_shouldReturnComments_whenEventIsDraftAndUserIsOwner() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        Event event = persistEvent(author);
        commentRepository.save(new Comment(CONTENT, author, event, LocalDateTime.now()));
        authenticateAs(author);

        List<CommentDto> comments = commentService.extractByEvent(event.getId());

        assertThat(comments).hasSize(1);
    }

    @Test
    void createComment_shouldThrow_whenEventIsNotAccessible() {
        User author = persistActiveUser("john.doe@test.com", "Doe", "John");
        User other = persistActiveUser("other@test.com", "Martin", "Paul");
        Event event = persistEvent(author);
        authenticateAs(other);

        assertThatThrownBy(() -> commentService.createComment(new CreateCommentRequest(event.getId(), CONTENT)))
                .isInstanceOf(NotFoundException.class);
        assertThat(commentRepository.findByEvent_IdOrderByCreatedAtAsc(event.getId())).isEmpty();
    }

    private User persistActiveUser(String email, String lastName, String firstName) {
        User user = new User();
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setPostalAddress("1 rue de Test");
        user.setEmail(email);
        user.setPassword("hashed-password");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.MEMBER);
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

    private Event persistPublishedEvent(User organizer) {
        Event event = persistEvent(organizer);
        event.publish();
        return eventRepository.save(event);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }
}
