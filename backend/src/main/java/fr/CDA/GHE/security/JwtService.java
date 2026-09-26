package fr.CDA.GHE.security;

import fr.CDA.GHE.entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Service chargé de la génération, de la lecture et de la validation des tokens JWT
 * utilisés pour l'authentification de l'application.
 */
@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;

  /**
   * Initialise la clé cryptographique utilisée pour signer/vérifier les JWT.
   *
   * @param secret       clé secrète (encodée en Base64) définie dans la configuration
   * @param expirationMs durée de validité d'un token, en millisecondes
   */
  public JwtService(@Value("${jwt.secret}") String secret,
                     @Value("${jwt.expiration-ms}") long expirationMs) {
    this.key = Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(secret)
    );
    this.expirationMs = expirationMs;
  }

  /**
   * Génère un JWT signé pour l'utilisateur donné.
   *
   * @param email adresse email de l'utilisateur (placée dans le sujet du token)
   * @param role  rôle de l'utilisateur (placé dans un claim personnalisé)
   * @return le JWT signé, prêt à être renvoyé au client
   */
  public String generateToken(String email, Role role) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .subject(email)
        .claim("role", role.name())
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();
  }

  /**
   * Extrait l'adresse email enregistrée dans le sujet du JWT.
   *
   * @param token JWT à analyser
   * @return l'adresse email contenue dans le token
   */
  public String extractEmail(String token) {
    return extractAllClaims(token).getSubject();
  }

  /**
   * Vérifie qu'un JWT est correctement signé et valide.
   *
   * @param token JWT à vérifier
   * @return {@code true} si le token est valide, {@code false} sinon
   */
  public boolean isTokenValid(String token) {
    try {
      extractAllClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException exception) {
      return false;
    }
  }

  /**
   * Extrait l'ensemble des informations contenues dans un JWT
   * après vérification de sa signature.
   *
   * @param token JWT à analyser
   * @return les informations contenues dans le token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}