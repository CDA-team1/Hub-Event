package fr.CDA.GHE.controller;


import fr.CDA.GHE.dto.CreateUserRequest;
import fr.CDA.GHE.dto.LoginRequest;
import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.AuthService;
import fr.CDA.GHE.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expose les routes d'authentification / inscription self-service de l'application.
 * <p>
 * La création de compte par un administrateur (membre affilié, organisateur, administrateur)
 * est une route distincte et réservée : voir {@link UserController}.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Connecte un utilisateur à partir de son email et de son mot de passe.
     *
     * @param request email et mot de passe saisis
     * @return 200 OK avec le jeton JWT et le rôle de l'utilisateur
     * @throws FunctionalException si les identifiants sont invalides ou si le compte
     *                             n'est pas actif/est suspendu
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws FunctionalException{
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Crée un compte membre non affilié (CU5, SFG §2.8) — parcours self-service,
     * accessible sans authentification.
     *
     * @param request informations saisies par le visiteur
     * @return 201 Created avec le compte créé
     * @throws FunctionalException si une règle métier n'est pas respectée
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto signup(@RequestBody CreateUserRequest request) throws FunctionalException {
        return userService.createUser(request);
    }

    /**
     * Active un compte à partir du lien reçu par email (CU6, SFG §2.9) — parcours
     * self-service, accessible sans authentification.
     *
     * @param token jeton d'activation transmis dans le lien
     * @return le compte activé
     * @throws FunctionalException si le lien est invalide ou si le compte ne peut plus être activé
     */
    @GetMapping("/activate")
    public UserDto activate(@RequestParam String token) throws FunctionalException {
        return userService.activateAccount(token);
    }

}
