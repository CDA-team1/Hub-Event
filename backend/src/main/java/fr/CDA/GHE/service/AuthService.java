package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.LoginRequest;
import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.exception.UnauthorizedException;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Gère l'authentification des utilisateurs (connexion par email/mot de passe).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                        AuthenticationManager authenticationManager,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Authentifie un utilisateur à partir de son email et de son mot de passe.
     * <p>
     * La vérification de l'email et du mot de passe est déléguée à l'{@link AuthenticationManager}
     * de Spring Security, qui s'appuie sur {@code JpaUserDetailsService} pour charger l'utilisateur
     * et sur le {@code PasswordEncoder} configuré pour comparer le mot de passe. En cas d'échec
     * (email inconnu ou mot de passe incorrect), Spring Security lève toujours la même exception
     * générique, ce qui évite qu'un attaquant ne puisse déterminer si un email est enregistré ou
     * non (énumération de comptes).
     * </p>
     * <p>
     * Une fois l'authentification réussie, deux règles métier supplémentaires sont vérifiées :
     * le compte doit être au statut {@link AccountStatus#ACTIVE} (donc ni inactif, ni anonymisé),
     * et il ne doit pas être suspendu (ou la suspension temporaire doit être terminée).
     * </p>
     *
     * @param request email et mot de passe saisis par l'utilisateur
     * @return le jeton JWT et le rôle de l'utilisateur authentifié
     * @throws UnauthorizedException si les identifiants sont invalides, si le compte
     *                               n'est pas actif, ou s'il est suspendu
     */
    public LoginResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Email ou mot de passe invalide");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe invalide"));

        if (user.getStatus() != AccountStatus.ACTIVE){
            throw new UnauthorizedException("Compte inactif ou anonymisé, connexion impossible");
        }

        if (user.isSuspended()){
            LocalDate endDate = user.getSuspensionEndDate();
            boolean stillBlocked = (endDate == null) || LocalDate.now().isBefore(endDate);
            if (stillBlocked){
                throw new UnauthorizedException("Compte suspendu");
            }
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return new LoginResponse(token, user.getRole());
    }
}
