package fr.CDA.GHE.entity.enums;

import org.springframework.security.core.GrantedAuthority;

/**
 * Rôle attribué à un utilisateur dans l'application.
 * Implémente {@link GrantedAuthority} pour servir directement d'autorité Spring Security.
 */
public enum Role implements GrantedAuthority {
  MEMBER,
  ORGANIZER,
  ADMIN;

  @Override
  public String getAuthority() {
    return "ROLE_" + name();
  }
}
