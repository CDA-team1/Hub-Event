package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.dto.CreateCommentRequest;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes commentaires (voir {@link ClubController} pour les règles générales).
 * <p>
 * Aucun {@code @Secured} ici : poster un commentaire est ouvert à tout compte connecté
 * (l'authentification est assurée par {@code anyRequest().authenticated()} côté SEC-01).
 * <p>
 * La lecture publique des commentaires se fait via l'endpoint imbriqué
 * {@code GET /events/{id}/comments} (décision SEC-02 = public), voir {@link EventController}.
 * Lister/consulter un commentaire isolé, le modifier ou le supprimer ne sont demandés par
 * aucun cas d'utilisation du SFG (CU12) : ces routes n'existent donc pas (API-04).
 */
@RestController
@RequestMapping("/comments")
public class CommentController implements CommentControllerDoc {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@RequestBody CreateCommentRequest request) throws FunctionalException {
        return commentService.createComment(request);
    }
}
