package fr.CDA.GHE.entity.enums;

/**
 * Statuts possibles d'un événement.
 */
public enum EventStatus {

  /**
   * Événement enregistré comme brouillon et non encore publié.
   */
  DRAFT,

  /**
   * Événement publié et visible.
   */
  PUBLISHED,

  /**
   * Événement annulé.
   */
  CANCELLED,

  /**
   * Événement terminé.
   */
  FINISHED
}
