package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.UserDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes utilisateurs (voir {@link ClubController} pour les règles générales).
 * <p>
 * Toutes les routes sont réservées à l'admin : la gestion des comptes existants (liste,
 * détail, modification, suppression), et la création d'un compte membre affilié,
 * organisateur ou administrateur (CdC §Création d'un compte — mot de passe temporaire,
 * pas encore implémentée). L'inscription self-service (CU5, MEMBER non affilié) est une
 * route distincte et publique : {@code POST /auth/signup}, voir {@link AuthController}.
 */
@RestController
@RequestMapping("/users")
public class UserController implements UserControllerDoc {

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping
    public PageDto<UserDto> getAll(Pageable pageable) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Utilisateur)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Utilisateur)");
    }

    // Réservé admin : création d'un membre affilié / organisateur / administrateur
    // (CdC §Création d'un compte). Pas confondre avec le signup self-service (POST /auth/signup).
    @Override
    @Secured("ROLE_ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto userDto) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature création admin)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto userDto) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Utilisateur)");
    }

    @Override
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Utilisateur)");
    }
}
