package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AdminUserRequest;
import fr.CDA.GHE.dto.ClubAffiliationRequest;
import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.dto.SuspendUserRequest;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.dto.AdminUserDto;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SEC-02 — Autorisations des routes utilisateurs (voir {@link ClubController} pour les règles générales).
 * <p>
 * Toutes les routes sont réservées à l'admin : gestion des comptes existants (liste,
 * détail, modification, suppression), et création d'un compte membre affilié,
 * organisateur ou administrateur (CU25, SFG §2.28 — mot de passe temporaire envoyé par
 * email). L'inscription self-service (CU5, MEMBER non affilié) est une route distincte et
 * publique : {@code POST /auth/signup}, voir {@link AuthController}.
 */
@RestController
@RequestMapping("/admin/users")
public class UserController implements UserControllerDoc {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping
    public PageDto<UserDto> getAll(
            @RequestParam(required = false) Role role,
            Pageable pageable
    ) {
        return userService.extractAll(role, pageable);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    public AdminUserDto getById(@PathVariable Long id) {
    return userService.extractById(id);
}

    @Override
    @Secured("ROLE_ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody AdminUserRequest request) throws FunctionalException {
        return userService.createUserByAdmin(request);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody AdminUserRequest request)
            throws FunctionalException {
        return userService.updateUserByAdmin(id, request);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteUserByAdmin(id);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/clubs")
    public List<ClubDto> updateAffiliations(
            @PathVariable Long id,
            @RequestBody ClubAffiliationRequest request
    ) throws FunctionalException {
        return userService.updateMemberAffiliations(id, request);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PostMapping("/{id}/suspension")
    public void suspend(
            @PathVariable Long id,
            @RequestBody SuspendUserRequest request
    ) throws FunctionalException {
        userService.suspendUser(id, request);
    }
}