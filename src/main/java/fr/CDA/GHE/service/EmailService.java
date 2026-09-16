package fr.CDA.GHE.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoie les emails transactionnels de l'application (SMTP).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie l'email de validation de compte (CU5 règle métier n°10, CU6).
     *
     * @param to             adresse email du destinataire
     * @param activationLink lien complet à cliquer pour activer le compte
     */
    public void sendActivationEmail(String to, String activationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Activez votre compte - Hub évènementiel");
        message.setText("""
                Bonjour,

                Merci de votre inscription sur le Hub évènementiel. Cliquez sur le lien \
                suivant pour activer votre compte :
                %s

                Ce lien est à usage unique.""".formatted(activationLink));

        mailSender.send(message);
        log.info("Email d'activation envoyé à {}", to);
    }

    /**
     * Envoie l'email contenant le mot de passe temporaire d'un compte créé par un
     * administrateur (CU25, SFG §2.28.1.4 : « un email contenant un mot de passe
     * temporaire est envoyé à la personne »).
     *
     * @param to                adresse email du destinataire
     * @param temporaryPassword mot de passe temporaire généré par le système
     */
    public void sendAdminCreatedAccountEmail(String to, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Votre compte a été créé - Hub évènementiel");
        message.setText("""
                Bonjour,

                Un compte vient d'être créé pour vous sur le Hub évènementiel par un administrateur.

                Mot de passe temporaire : %s

                Connectez-vous avec cet identifiant pour accéder à votre compte.""".formatted(temporaryPassword));

        mailSender.send(message);
        log.info("Email de création de compte (admin) envoyé à {}", to);
    }

    /**
     * Envoie l'email de promotion depuis la liste d'attente (CU9 règle métier n°7, CU10 règle métier n°5).
     *
     * @param to         adresse email du destinataire
     * @param eventTitle titre de l'évènement concerné
     */
    public void sendWaitingListPromotionEmail(String to, String eventTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Vous êtes inscrit - Hub évènementiel");
        message.setText("""
                Bonjour,

                Une place s'est libérée pour l'évènement "%s" et vous étiez le premier \
                sur la liste d'attente : vous êtes désormais inscrit !""".formatted(eventTitle));

        mailSender.send(message);
        log.info("Email de promotion (liste d'attente) envoyé à {} pour l'évènement {}", to, eventTitle);
    }

    /**
     * Envoie l'email d'annulation d'inscription par l'organisateur, avec le motif (CU22 règle métier).
     *
     * @param to         adresse email du destinataire
     * @param eventTitle titre de l'évènement concerné
     * @param reason     motif de l'annulation, saisi par l'organisateur
     */
    public void sendRegistrationCancelledEmail(String to, String eventTitle, String reason){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Votre inscription a été annulée - Hub évènementiel");
        message.setText("""
                Bonjour)
                
                Votre inscription à l'évènement "%s" a été annulée par l'organisateur.
                
                Motif : %s""".formatted(eventTitle, reason));

        mailSender.send(message);
        log.info("Email d'annulation d'inscription (organisateur) envoyé à {} pour l'évènement {}", to, eventTitle);
    }
}
