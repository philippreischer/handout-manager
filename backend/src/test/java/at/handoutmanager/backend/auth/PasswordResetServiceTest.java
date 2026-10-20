package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.security.RefreshTokenService;
import at.handoutmanager.backend.security.RevocationReason;
import at.handoutmanager.backend.security.SecureTokenUtil;
import at.handoutmanager.backend.user.User;
import at.handoutmanager.backend.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "max@mustermann.com";
    private static final String UNKNOWN_EMAIL = "unknown@example.com";
    private static final String USER_NAME = "Max";
    private static final String PASSWORD_HASH = "hashed-pass";
    private static final String NEW_PASSWORD = "newpass1234";
    private static final String RAW_TOKEN = "some-raw-token";
    private static final String RESET_URL = "http://localhost:5173/reset-password";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    @Mock
    private UserService userService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<PasswordResetRequestedEvent> eventCaptor;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    private PasswordResetService sut;

    @BeforeEach
    void setUp() {
        PasswordResetProperties properties = new PasswordResetProperties();
        properties.setTokenTtl(TOKEN_TTL);
        properties.setResetUrl(RESET_URL);

        sut = new PasswordResetService(userService, passwordResetTokenRepository,
                refreshTokenService, eventPublisher, properties);
    }

    // Requesting a reset

    @Test
    void shouldDoNothing_whenEmailIsUnknown() {
        when(userService.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

        sut.requestReset(UNKNOWN_EMAIL);

        verify(passwordResetTokenRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldPublishLinkMatchingTheStoredHash_whenRequestingReset() {
        when(userService.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser()));

        sut.requestReset(EMAIL);

        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        PasswordResetRequestedEvent event = eventCaptor.getValue();
        String sentToken = event.resetUrl().replace(RESET_URL + "?token=", "");

        // The address in the event is the stored, normalised one - not the input.
        assertThat(event.recipient()).isEqualTo(EMAIL);
        assertThat(SecureTokenUtil.hash(sentToken))
                .isEqualTo(tokenCaptor.getValue().getTokenHash());
    }

    @Test
    void shouldStoreHashInsteadOfRawToken_whenRequestingReset() {
        when(userService.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser()));

        sut.requestReset(EMAIL);

        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetToken stored = tokenCaptor.getValue();

        assertThat(stored.getTokenHash()).hasSize(64);
        assertThat(stored.getUserId()).isEqualTo(USER_ID);
        assertThat(stored.getExpiresAt()).isAfter(Instant.now());
        assertThat(stored.getUsedAt()).isNull();
    }

    @Test
    void shouldInvalidateOpenTokens_whenRequestingResetAgain() {
        List<PasswordResetToken> openTokens = List.of(activeToken(), activeToken());
        when(userService.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser()));
        when(passwordResetTokenRepository.findByUserIdAndUsedAtIsNull(USER_ID))
                .thenReturn(openTokens);

        sut.requestReset(EMAIL);

        verify(passwordResetTokenRepository).saveAll(openTokens);
        assertThat(openTokens).allSatisfy(token -> assertThat(token.getUsedAt()).isNotNull());
    }

    // Confirming a reset

    @Test
    void shouldSetPasswordThenRevokeTokens_whenConfirmingReset() {
        PasswordResetToken token = activeToken();
        when(passwordResetTokenRepository.findByTokenHash(SecureTokenUtil.hash(RAW_TOKEN)))
                .thenReturn(Optional.of(token));

        sut.confirmReset(RAW_TOKEN, NEW_PASSWORD);

        InOrder inOrder = inOrder(userService, passwordResetTokenRepository, refreshTokenService);
        inOrder.verify(userService).resetPassword(USER_ID, NEW_PASSWORD);
        inOrder.verify(passwordResetTokenRepository).save(token);
        inOrder.verify(refreshTokenService).revokeAllTokens(USER_ID, RevocationReason.PASSWORD_CHANGED);

        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    void shouldThrowAndChangeNothing_whenTokenIsUnknown() {
        when(passwordResetTokenRepository.findByTokenHash(SecureTokenUtil.hash(RAW_TOKEN)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.confirmReset(RAW_TOKEN, NEW_PASSWORD))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        verify(userService, never()).resetPassword(any(), any());
        verify(refreshTokenService, never()).revokeAllTokens(any(), any());
    }

    @Test
    void shouldThrow_whenTokenWasAlreadyUsed() {
        when(passwordResetTokenRepository.findByTokenHash(SecureTokenUtil.hash(RAW_TOKEN)))
                .thenReturn(Optional.of(usedToken()));

        assertThatThrownBy(() -> sut.confirmReset(RAW_TOKEN, NEW_PASSWORD))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        verify(userService, never()).resetPassword(any(), any());
    }

    @Test
    void shouldThrow_whenTokenIsExpired() {
        when(passwordResetTokenRepository.findByTokenHash(SecureTokenUtil.hash(RAW_TOKEN)))
                .thenReturn(Optional.of(expiredToken()));

        assertThatThrownBy(() -> sut.confirmReset(RAW_TOKEN, NEW_PASSWORD))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        verify(userService, never()).resetPassword(any(), any());
    }

    private User existingUser() {
        User user = new User(EMAIL, PASSWORD_HASH, USER_NAME);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }

    private PasswordResetToken activeToken() {
        return new PasswordResetToken(USER_ID, SecureTokenUtil.hash(RAW_TOKEN),
                Instant.now().plus(ONE_HOUR));
    }

    private PasswordResetToken expiredToken() {
        return new PasswordResetToken(USER_ID, SecureTokenUtil.hash(RAW_TOKEN),
                Instant.now().minus(ONE_HOUR));
    }

    private PasswordResetToken usedToken() {
        PasswordResetToken token = activeToken();
        token.markUsed(Instant.now());
        return token;
    }
}
