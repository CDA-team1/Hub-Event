package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.CreateCommentRequest;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.CommentService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes commentaires (voir {@link ClubController} pour les règles générales).
 * <p>
 * Aucun {@code @Secured} ici : poster/éditer/supprimer est ouvert à tout compte connecté
 * (l'authentification est assurée par {@code anyRequest().authenticated()} côté SEC-01).
 * <p>
 * {@code update}/{@code delete} : contrôle de <strong>propriété</strong> (auteur) ou rôle ADMIN
 * (modération) à faire dans le service → 403 sinon. Hors scope COM-02, restent en TODO.
 * <p>
 * La lecture publique des commentaires se fait via l'endpoint imbriqué
 * {@code GET /events/{id}/comments} (décision SEC-02 = public), voir {@link EventController}.
 */
@RestController
@RequestMapping("/comments")
public class CommentController implements CommentControllerDoc {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    @GetMapping
    public PageDto<CommentDto> getAll(Pageable pageable) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Commentaire)");
    }

    @Override
    @GetMapping("/{id}")
    public CommentDto getById(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Commentaire)");
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@RequestBody CreateCommentRequest request) throws FunctionalException {
        return commentService.createComment(request);
    }

    @Override
    @PutMapping("/{id}")
    public CommentDto update(@PathVariable Long id, @RequestBody CommentDto commentDto) {
        // TODO SEC-02 : contrôle de propriété (auteur) ou ADMIN → 403 sinon
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Commentaire)");
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // TODO SEC-02 : contrôle de propriété (auteur) ou ADMIN (modération) → 403 sinon
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Commentaire)");
    }
}
