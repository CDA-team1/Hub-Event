package fr.CDA.GHE.service;

/**
 * Envoi des emails transactionnels de l'application.
 */

public interface EmailService {

    /**
     * Notifie un utilisateur qu'il vient d'être promu de la liste d'attente
     * vers une inscription confirmée à un évènement.
     *
     * @param to         adresse email du destinataire
     * @param eventTitle titre de l'évènement concerné
     */
    void sendWaitingListPromotionEmail(String to, String eventTitle);
}
