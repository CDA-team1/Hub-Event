package fr.CDA.GHE.security;

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

        UserDetails userDetails =
            userDetailsService.loadUserByUsername(email);

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

    filterChain.doFilter(request, response);
  }
}
