package fr.CDA.GHE.controller;


import fr.CDA.GHE.dto.LoginRequest;
import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expose les routes d'authentification de l'application.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

}
