package fr.CDA.GHE.security;

import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
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

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

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

        UserDetails userDetails =
            userDetailsService.loadUserByUsername(email);

        if (isAccountUsable(userDetails)) {
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
      log.info("REFUS authentification JWT (compte non actif) : email={}", user.getEmail());
      return false;
    }

    if (user.isSuspended()) {
      LocalDate endDate = user.getSuspensionEndDate();
      boolean stillBlocked = endDate == null || LocalDate.now().isBefore(endDate);
      if (stillBlocked) {
        log.info("REFUS authentification JWT (compte suspendu) : email={}", user.getEmail());
        return false;
      }
    }

    return true;
  }
}
