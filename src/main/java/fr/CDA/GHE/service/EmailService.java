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
}
