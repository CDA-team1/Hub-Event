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
}
