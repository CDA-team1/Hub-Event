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
 * Note : {@code POST /users} (création de compte) est <strong>public</strong> → pas d'annotation.
 * La modification de son propre profil ({@code /users/me}) est un endpoint distinct, non présent
 * dans {@link UserControllerDoc} : à sécuriser (authentifié) quand il sera créé.
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

    // Public : création de compte
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto userDto) {
        throw new UnsupportedOperationException("TODO logique métier (tâche feature Utilisateur)");
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
