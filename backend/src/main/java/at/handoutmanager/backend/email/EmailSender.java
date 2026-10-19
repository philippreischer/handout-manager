package at.handoutmanager.backend.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends outgoing mail. Takes plain values only - no entities: once sending moves
 * to a background thread there is no Hibernate session left, and a detached
 * entity would fail there with no visible error.
 */
@Component
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;

    public EmailSender(JavaMailSender javaMailSender, EmailProperties emailProperties) {
        this.javaMailSender = javaMailSender;
        this.emailProperties = emailProperties;
    }

    public void sendPasswordReset(String recipient, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getFrom());
        message.setTo(recipient);
        message.setSubject("Reset your handout-manager password");
        message.setText("""
                Someone asked to reset the password for this address.

                Open this link to choose a new one:
                %s

                The link is valid for a short time and can be used once. If this
                was not you, no action is needed - your password stays unchanged.
                """.formatted(resetUrl));

        send(message);
    }

    /**
     * Failures are logged, never rethrown. A failed send must not change the
     * caller's response: the request endpoint answers 204 either way, and an
     * error there would reveal which addresses are registered.
     */
    private void send(SimpleMailMessage message) {
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            log.error("Could not send mail with subject '{}'", message.getSubject(), e);
        }
    }
}
