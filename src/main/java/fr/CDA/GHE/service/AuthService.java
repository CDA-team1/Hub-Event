package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.LoginRequest;
import fr.CDA.GHE.dto.LoginResponse;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Gère l'authentification des utilisateurs (connexion par email/mot de passe).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authentifie un utilisateur à partir de son email et de son mot de passe.
     * <p>
     * Règles vérifiées, dans l'ordre : l'email doit correspondre à un compte existant,
     * le mot de passe doit correspondre au mot de passe hashé en base, le compte doit
     * être au statut {@link AccountStatus#ACTIVE} (donc ni inactif, ni anonymisé), et le
     * compte ne doit pas être suspendu (ou la suspension temporaire doit être terminée).
     * </p>
     * <p>
     * Les échecs liés à l'email et au mot de passe renvoient volontairement le même
     * message d'erreur générique, afin d'éviter qu'un attaquant ne puisse déterminer
     * si un email est enregistré ou non (énumération de comptes).
     * </p>
     *
     * @param request email et mot de passe saisis par l'utilisateur
     * @return le jeton JWT et le rôle de l'utilisateur authentifié
     * @throws FunctionalException si les identifiants sont invalides, si le compte
     *                             n'est pas actif, ou s'il est suspendu
     */
    public LoginResponse login(LoginRequest request) throws FunctionalException {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new FunctionalException("Email ou mot de passe invalide"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new FunctionalException("Email ou mot de passe invalide");
        }

        if (user.getStatus() != AccountStatus.ACTIVE){
            throw new FunctionalException("Compte inactif ou anonymisé, connexion impossible");
        }

        if (user.isSuspended()){
            LocalDate endDate = user.getSuspensionEndDate();
            boolean stillBlocked = (endDate == null) || LocalDate.now().isBefore(endDate);
            if (stillBlocked){
                throw new FunctionalException("Compte suspendu");
            }
        }

        String token = "TODO"; // A remplacer une fois JwtService prêt

        return new LoginResponse(token, user.getRole());
    }
}