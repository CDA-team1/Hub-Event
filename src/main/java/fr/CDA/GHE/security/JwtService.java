package fr.CDA.GHE.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Service chargé de la lecture et de la validation des tokens JWT
 * utilisés pour l'authentification de l'application.
 */
@Service
public class JwtService {

  private final SecretKey key;

  /**
   * Initialise la clé cryptographique utilisée pour vérifier les JWT.
   *
   * @param secret clé secrète définie dans la configuration de l'application
   */
  public JwtService(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(secret)
    );
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