package fr.CDA.GHE.config;

import fr.CDA.GHE.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de la sécurité (auth JWT, stateless).
 * securedEnabled=true : active @Secured sur les controllers (SEC-02).
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** Utilisé par AuthService pour vérifier les identifiants au login. */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter
  ) throws Exception {

    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            // Routes publiques (docs/SEC-02-matrice-routes.md)
            .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/signup").permitAll()
            .requestMatchers(HttpMethod.GET, "/auth/activate", "/auth/confirm-password-change").permitAll()
            .requestMatchers(HttpMethod.GET, "/events", "/events/*").permitAll()
            .requestMatchers(HttpMethod.GET, "/events/*/images/*").permitAll()
            .requestMatchers(HttpMethod.GET, "/events/*/comments").permitAll()
            .requestMatchers(HttpMethod.GET, "/events/search").permitAll() // pas encore codé
            .requestMatchers(HttpMethod.GET, "/clubs", "/clubs/*").permitAll()
            .requestMatchers(HttpMethod.GET, "/documents/*", "/documents/*/pdf").permitAll()
            .requestMatchers("/error").permitAll() // sinon écrase le code d'erreur d'origine
            .anyRequest().authenticated())
        // Sans httpBasic/formLogin, Spring renverrait 403 par défaut sans jeton -> on force le 401
        .exceptionHandling(ex -> ex.authenticationEntryPoint(
            (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise")))
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );

    return http.build();

  }
}
