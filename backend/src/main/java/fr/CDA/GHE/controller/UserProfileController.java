package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.UpdateUserRequest;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.dto.UserProfileDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.UserService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SEC-02 — Autorisations des routes du profil de l'utilisateur connecté (CU13).
 * <p>
 * Aucune restriction de rôle : tout utilisateur connecté modifie SON propre compte,
 * identifié via le JWT (jamais un id transmis par le client). Distinct de la gestion admin
 * des comptes ({@code /admin/users}), voir {@link UserController}.
 */
@RestController
@RequestMapping("/users/me")
public class UserProfileController implements UserProfileControllerDoc {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @Override
    @GetMapping
    public UserProfileDto getOwnAccount() throws FunctionalException {
        return userService.getOwnAccount();
    }

    @Override
    @PutMapping
    public UserDto update(@RequestBody UpdateUserRequest request) throws FunctionalException {
        return userService.updateOwnAccount(request);
    }
}
