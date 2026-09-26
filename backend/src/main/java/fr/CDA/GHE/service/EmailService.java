package fr.CDA.GHE.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
   * administrateur, ainsi que le lien à cliquer pour le finaliser (CU25/CPT-06, CdC §
   * « Validation de la création d'un compte ») : le compte reste INACTIF tant que ce lien
   * n'a pas été utilisé pour saisir ce mot de passe temporaire et en choisir un définitif.
   *
   * @param to                 adresse email du destinataire
   * @param temporaryPassword  mot de passe temporaire généré par le système
   * @param confirmationLink   lien complet à cliquer pour finaliser le compte
   */
  public void sendAdminCreatedAccountEmail(String to, String temporaryPassword, String confirmationLink) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("Votre compte a été créé - Hub évènementiel");
    message.setText("""
        Bonjour,

        Un compte vient d'être créé pour vous sur le Hub évènementiel par un administrateur.

        Mot de passe temporaire : %s

        Cliquez sur le lien suivant pour finaliser votre compte, en saisissant ce mot de \
        passe temporaire puis un mot de passe définitif :
        %s

        Ce lien est à usage unique. Vous ne pourrez pas vous connecter tant que votre \
        compte n'est pas finalisé.""".formatted(temporaryPassword, confirmationLink));

    mailSender.send(message);
    log.info("Email de création de compte (admin) envoyé à {}", to);
  }

  /**
   * Envoie le lien de confirmation d'un changement de mot de passe (CU13 ; CdC : « Si je ne
   * confirme pas en cliquant sur le lien, mon mot de passe n'est pas modifié »). Le nouveau
   * mot de passe n'est appliqué qu'au clic sur ce lien, jamais avant.
   *
   * @param to               adresse email du destinataire
   * @param confirmationLink lien complet à cliquer pour confirmer le changement
   */
  public void sendPasswordChangeConfirmationEmail(String to, String confirmationLink) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("Confirmez le changement de votre mot de passe - Hub évènementiel");
    message.setText("""
        Bonjour,
        
        Vous avez demandé à modifier le mot de passe de votre compte sur le Hub \
        évènementiel. Cliquez sur le lien suivant pour confirmer ce changement :
        %s
        
        Si vous n'êtes pas à l'origine de cette demande, ignorez cet email : votre \
        mot de passe actuel reste inchangé tant que vous n'avez pas cliqué sur ce lien.""".formatted(confirmationLink));

    mailSender.send(message);
    log.info("Email de confirmation de changement de mot de passe envoyé à {}", to);
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
  public void sendRegistrationCancelledEmail(String to, String eventTitle, String reason) {
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

  /**
   * Envoie l'email d'annulation d'un événement aux inscrits et aux personnes
   * présentes sur la liste d'attente (CU20).
   *
   * @param to         adresse email du destinataire
   * @param eventTitle titre de l'évènement annulé
   */
  public void sendEventCancelledEmail(String to, String eventTitle) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("Évènement annulé - Hub évènementiel");
    message.setText("""
        Bonjour,
        
        L'évènement "%s" a été annulé par son organisateur.
        """.formatted(eventTitle));

    mailSender.send(message);
    log.info(
        "Email d'annulation d'évènement envoyé à {} pour l'évènement {}",
        to,
        eventTitle
    );
  }

  /**
   * Envoie l'email de notification de suspension d'un compte (SUSP-01), avec le motif et
   * la durée (définitive ou jusqu'à une date donnée).
   *
   * @param to      adresse email du destinataire
   * @param reason  motif de la suspension
   * @param endDate date de fin de la suspension, {@code null} si définitive
   */
  public void sendAccountSuspendedEmail(String to, String reason, LocalDate endDate) {
    String duration = endDate == null
        ? "de manière définitive"
        : "jusqu'au " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("Votre compte a été suspendu - Hub évènementiel");
    message.setText("""
        Bonjour,

        Votre compte a été suspendu %s.

        Motif : %s""".formatted(duration, reason));

    mailSender.send(message);
    log.info("Email de suspension de compte envoyé à {}", to);
  }
}
