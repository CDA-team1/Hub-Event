package fr.CDA.GHE.service;

import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service permettant à Spring Security de récupérer
 * un utilisateur à partir de son adresse email.
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Initialise le service avec le repository des utilisateurs.
   *
   * @param userRepository repository permettant d'accéder aux utilisateurs
   */
  public JpaUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Recherche un utilisateur à partir de son adresse email
   * et le convertit en utilisateur reconnu par Spring Security.
   *
   * @param email adresse email utilisée comme identifiant de connexion
   * @return les informations de l'utilisateur pour Spring Security
   * @throws UsernameNotFoundException si aucun utilisateur ne possède cet email
   */
  @Override
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {

    User user = userRepository.findByEmail(email)
        .orElseThrow(() ->
            new UsernameNotFoundException("Utilisateur introuvable.")
        );

    return org.springframework.security.core.userdetails.User
        .withUsername(user.getEmail())
        .password(user.getPassword())
        .roles(user.getRole().name())
        .build();
  }
}
