package com.example.accessingdatamysql.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails for the authentication flow.
 *
 * <p>Currently only used to deliver password-reset links. Uses Spring's
 * {@link JavaMailSender}, which is configured from the {@code spring.mail.*}
 * properties (SMTP host, credentials, etc.).</p>
 */
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    /**
     * @param mailSender  the configured JavaMailSender
     * @param fromAddress the "from" address shown on outgoing mail, taken from
     *                    {@code app.mail.from}
     */
    public MailService(JavaMailSender mailSender,
                       @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * Sends a password-reset email containing a clickable link.
     *
     * <p>Sent as plain text on purpose: it is simple, renders in every client,
     * and avoids pulling in an HTML templating dependency. The link itself
     * opens the reset page served by the backend.</p>
     *
     * @param toEmail  the recipient's email address
     * @param resetUrl the fully-formed reset link, including the token
     */
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Återställ ditt lösenord – Skogsjakten");
        message.setText(
                "Hej!\n\n"
                        + "Vi fick en begäran om att återställa lösenordet för ditt "
                        + "Skogsjakten-konto.\n\n"
                        + "Klicka på länken nedan för att välja ett nytt lösenord. "
                        + "Länken är giltig i 30 minuter och kan bara användas en gång:\n\n"
                        + resetUrl + "\n\n"
                        + "Om du inte begärde detta kan du ignorera det här mejlet – "
                        + "ditt lösenord ändras inte.\n\n"
                        + "Hälsningar,\nSkogsjakten");
        mailSender.send(message);
    }
}