package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.email.EmailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class PasswordResetMailListener {

    private final EmailSender emailSender;

    public PasswordResetMailListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    // No @Transactional here: the transaction is already committed, and opening a new
    // one would hold a database connection for as long as the SMTP call takes.
    // No fallbackExecution either - a missing @Transactional on the publishing service
    // must fail loudly as "no mail arrives", not quietly as "no ordering guarantee".
    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        emailSender.sendPasswordReset(event.recipient(), event.resetUrl());
    }
}
