package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.CreateCommentRequest;
import fr.CDA.GHE.entity.Comment;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.CommentMapper;
import fr.CDA.GHE.repository.CommentRepository;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.util.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gère les commentaires publiés sur les événements (CU12, SFG §2.15).
 */
@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository, EventRepository eventRepository,
                           UserRepository userRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    /**
     * Publie un commentaire sur un événement pour l'utilisateur connecté (CU12, règles n°1 à 3).
     *
     * @param request événement commenté et contenu saisi
     * @return le commentaire créé
     * @throws FunctionalException si le compte n'est pas actif, si le contenu est vide, ou si
     *                             l'événement n'existe pas
     */
    @Transactional
    public CommentDto createComment(CreateCommentRequest request) throws FunctionalException {
        if (request.content() == null || request.content().isBlank()) {
            throw new FunctionalException("Veuillez saisir un commentaire.");
        }

        User author = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (author.getStatus() != AccountStatus.ACTIVE) {
            throw new FunctionalException("Votre compte doit être actif pour publier un commentaire.");
        }

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new NotFoundException("Événement introuvable"));

        Comment comment = new Comment(request.content(), author, event, LocalDateTime.now());
        Comment saved = commentRepository.save(comment);

        log.info("AJOUT commentaire : id={} eventId={} authorId={}", saved.getId(), event.getId(), author.getId());

        return commentMapper.toDto(saved);
    }

    /**
     * Retourne les commentaires d'un événement, du plus ancien au plus récent (CU12, règle n°3).
     * Consultation publique, comme le détail de l'événement.
     *
     * @param eventId identifiant de l'événement
     * @return la liste des commentaires de l'événement
     * @throws NotFoundException si l'événement n'existe pas
     */
    @Transactional(readOnly = true)
    public List<CommentDto> extractByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Événement introuvable");
        }

        List<Comment> comments = commentRepository.findByEvent_IdOrderByCreatedAtAsc(eventId);
        return commentMapper.toDtoList(comments);
    }
}
