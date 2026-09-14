package fr.CDA.GHE.service;

import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant à Spring Security de récupérer
 * un utilisateur à partir de son adresse email.
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(JpaUserDetailsService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Initialise le service avec le repository des utilisateurs et l'encodeur de mot de passe.
   *
   * @param userRepository  repository permettant d'accéder aux utilisateurs
   * @param passwordEncoder encodeur utilisé pour hasher les mots de passe des comptes de démo
   */
  public JpaUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Recherche un utilisateur à partir de son adresse email.
   * L'entité {@link User} implémente {@link UserDetails} : elle est renvoyée telle quelle,
   * aucune conversion nécessaire.
   *
   * @param email adresse email utilisée comme identifiant de connexion
   * @return l'utilisateur, servant directement de principal Spring Security
   * @throws UsernameNotFoundException si aucun utilisateur ne possède cet email
   */
  @Override
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {

    return userRepository.findByEmail(email)
        .orElseThrow(() ->
            new UsernameNotFoundException("Utilisateur introuvable.")
        );
  }

  /**
   * Crée deux comptes de démo (un MEMBER, un ADMIN) si la table est vide.
   * <p>
   * Permet de tester {@code POST /auth/login} (Postman, base locale) avant que le signup
   * ({@code POST /users}) soit implémenté. Le mot de passe n'est jamais stocké en clair :
   * {@link PasswordEncoder} en garde une empreinte BCrypt, comparée à la saisie au login.
   * <p>
   * Appelée par {@code DataConfig}, qui décide s'il faut l'exécuter.
   */
  @Transactional
  public void initData() {
    if (userRepository.count() > 0) {
      return;
    }

    User member = new User();
    member.setFirstName("John");
    member.setLastName("Doe");
    member.setEmail("member.demo@ghe.local");
    member.setPostalAddress("1 rue de la Fédération, 31000 Toulouse");
    member.setPassword(passwordEncoder.encode("Member123!"));
    member.setStatus(AccountStatus.ACTIVE);
    member.setRole(Role.MEMBER);
    userRepository.save(member);

    User admin = new User();
    admin.setFirstName("Elliot");
    admin.setLastName("Alderson");
    admin.setEmail("admin.demo@ghe.local");
    admin.setPostalAddress("1 rue de la Fédération, 31000 Toulouse");
    admin.setPassword(passwordEncoder.encode("Admin123!"));
    admin.setStatus(AccountStatus.ACTIVE);
    admin.setRole(Role.ADMIN);
    userRepository.save(admin);

    log.info("Comptes de démo créés : {} (MEMBER), {} (ADMIN).", member.getEmail(), admin.getEmail());
  }
}
