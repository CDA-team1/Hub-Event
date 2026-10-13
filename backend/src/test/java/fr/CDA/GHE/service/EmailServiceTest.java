package fr.CDA.GHE.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests de {@link EmailService} sans contexte Spring : l'exécution en tâche de fond ({@code @Async})
 * n'est pas active ici, les méthodes s'exécutent directement.
 * <p>
 * Distinction testée : les notifications ne propagent jamais une panne SMTP, les emails
 * indispensables (activation, mot de passe) la propagent.
 * </p>
 */
class EmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final EmailService emailService = new EmailService(mailSender);

    private void smtpIsDown() {
        doThrow(new MailSendException("SMTP injoignable")).when(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEventCancelledEmail_shouldSendMessageToRecipient() {
        emailService.sendEventCancelledEmail("member@test.com", "Tournoi");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).containsExactly("member@test.com");
        assertThat(captor.getValue().getText()).contains("Tournoi");
    }

    @Test
    void notifications_shouldNotThrow_whenSmtpIsDown() {
        smtpIsDown();

        assertThatCode(() -> {
            emailService.sendEventCancelledEmail("member@test.com", "Tournoi");
            emailService.sendWaitingListPromotionEmail("member@test.com", "Tournoi");
            emailService.sendRegistrationCancelledEmail("member@test.com", "Tournoi", "Motif");
            emailService.sendAccountSuspendedEmail("member@test.com", "Motif", LocalDate.now().plusDays(3));
        }).doesNotThrowAnyException();
    }

    @Test
    void activationEmail_shouldPropagateFailure_whenSmtpIsDown() {
        smtpIsDown();

        assertThatThrownBy(() -> emailService.sendActivationEmail("member@test.com", "http://lien"))
                .isInstanceOf(MailSendException.class);
    }

    @Test
    void passwordChangeAndAdminAccountEmails_shouldPropagateFailure_whenSmtpIsDown() {
        smtpIsDown();

        assertThatThrownBy(() -> emailService.sendPasswordChangeConfirmationEmail("member@test.com", "http://lien"))
                .isInstanceOf(MailSendException.class);
        assertThatThrownBy(() -> emailService.sendAdminCreatedAccountEmail("member@test.com", "Temp123!", "http://lien"))
                .isInstanceOf(MailSendException.class);
    }
}
