package fr.CDA.GHE.service;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;

@Service
public class LoggingEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendWaitingListPromotionEmail(String to, String eventTitle){
        log.info("[EMAIL] Promotion depuis la liste d'attente pour {} - évènement : {}", to, eventTitle);
    }
}
