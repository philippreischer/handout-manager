package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.email.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetMailListenerTest {

    private static final String RECIPIENT = "max@mustermann.com";
    private static final String RESET_URL =
            "http://localhost:5173/reset-password?token=some-raw-token";

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private PasswordResetMailListener sut;

    /**
     * Both parameters of sendPasswordReset are Strings, so swapping them compiles and
     * would mail the token to a URL as the address. This pins their order. Whether the
     * annotations actually defer the call needs the full context - see the integration
     * test still to be written.
     */
    @Test
    void shouldForwardRecipientAndUrlInOrder_whenEventIsReceived() {
        sut.onPasswordResetRequested(new PasswordResetRequestedEvent(RECIPIENT, RESET_URL));

        verify(emailSender).sendPasswordReset(RECIPIENT, RESET_URL);
    }
}
