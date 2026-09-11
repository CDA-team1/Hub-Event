package fr.CDA.GHE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import fr.CDA.GHE.security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de la sécurité de l'application.
 * Centralise les composants nécessaires à l'authentification
 * et à la sécurisation des requêtes HTTP.
 */
@Configuration
public class SecurityConfig {


  /**
   * Fournit l'encodeur utilisé pour sécuriser les mots de passe.
   *
   * @return une instance de {@link BCryptPasswordEncoder}
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configure la chaîne de filtres de sécurité de l'application.
   * Désactive la protection CSRF, configure l'application sans session HTTP
   * et ajoute le filtre d'authentification JWT.
   *
   * @param http                    configuration de la sécurité HTTP
   * @param jwtAuthenticationFilter filtre chargé de traiter les JWT
   * @return la chaîne de filtres de sécurité configurée
   * @throws Exception si la configuration de sécurité échoue
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter
  ) throws Exception {

    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth ->
            auth.anyRequest().authenticated())
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );

    return http.build();

  }
}
