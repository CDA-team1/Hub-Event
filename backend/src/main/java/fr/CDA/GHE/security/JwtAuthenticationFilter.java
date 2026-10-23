package fr.CDA.GHE.security;

import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Filtre chargé de traiter l'authentification JWT
 * pour chaque requête HTTP reçue par l'application.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  /**
   * Initialise le filtre avec les services nécessaires
   * à la validation du JWT et au chargement de l'utilisateur.
   *
   * @param jwtService         service chargé de lire et valider les JWT
   * @param userDetailsService service chargé de récupérer les informations de l'utilisateur
   */
  public JwtAuthenticationFilter(
      JwtService jwtService,
      UserDetailsService userDetailsService
  ) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Intercepte chaque requête HTTP afin d'examiner
   * les informations d'authentification qu'elle contient.
   *
   * @param request     requête HTTP reçue
   * @param response    réponse HTTP associée
   * @param filterChain chaîne de filtres à poursuivre
   * @throws ServletException si une erreur survient pendant le filtrage
   * @throws IOException      si une erreur d'entrée/sortie survient
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader != null
        && authorizationHeader.startsWith("Bearer ")
        && SecurityContextHolder.getContext().getAuthentication() == null) {

      String token = authorizationHeader.substring(7);

      if (jwtService.isTokenValid(token)) {
        String email = jwtService.extractEmail(token);

        UserDetails userDetails = findUser(email);

        if (userDetails != null && isAccountUsable(userDetails)) {
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails,
                  null,
                  userDetails.getAuthorities()
              );

          SecurityContextHolder.getContext()
              .setAuthentication(authentication);
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Charge le compte désigné par le jeton. Un jeton encore valide peut pointer vers un compte qui
   * n'existe plus sous cet email (typiquement un compte anonymisé : son email a été remplacé) :
   * la requête est alors traitée comme anonyme, ce qui donne un 401 sur les routes protégées —
   * et non une 500, l'exception ne pouvant pas être traitée par le contrôleur à ce stade.
   *
   * @param email email lu dans le jeton
   * @return le compte, {@code null} s'il n'existe pas
   */
  private UserDetails findUser(String email) {
    try {
      return userDetailsService.loadUserByUsername(email);
    } catch (UsernameNotFoundException exception) {
      SecurityLog.tokenRefused("compte introuvable", null);
      return null;
    }
  }

  /**
   * Revérifie, à chaque requête, les mêmes règles que {@code AuthService.login} (compte
   * {@link AccountStatus#ACTIVE} et non suspendu) — sans quoi un compte suspendu ou anonymisé
   * après l'émission d'un JWT garderait l'accès jusqu'à l'expiration de ce jeton (jusqu'à 1h,
   * voir {@code jwt.expiration-ms}).
   *
   * @param userDetails principal chargé pour la requête courante (l'entité {@link User} elle-même)
   * @return {@code true} si le compte peut être authentifié pour cette requête
   */
  private boolean isAccountUsable(UserDetails userDetails) {
    if (!(userDetails instanceof User user)) {
      return true; // ne devrait jamais arriver, voir JpaUserDetailsService
    }

    if (user.getStatus() != AccountStatus.ACTIVE) {
      SecurityLog.tokenRefused("compte non actif", user.getId());
      return false;
    }

    if (user.isSuspended()) {
      LocalDate endDate = user.getSuspensionEndDate();
      boolean stillBlocked = endDate == null || LocalDate.now().isBefore(endDate);
      if (stillBlocked) {
        SecurityLog.tokenRefused("compte suspendu", user.getId());
        return false;
      }
    }

    return true;
  }
}
