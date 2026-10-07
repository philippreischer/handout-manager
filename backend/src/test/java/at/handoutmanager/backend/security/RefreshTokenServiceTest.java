package at.handoutmanager.backend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Long USER_ID = 42L;
    private static final String TOKEN_HASH = "some-hash";
    private static final String RAW_TOKEN = "some-raw-value";
    private static final Duration ONE_DAY = Duration.ofDays(1);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenService sut;

    @Captor
    private ArgumentCaptor<RefreshToken> tokenCaptor;

    @Test
    void shouldStoreHashInsteadOfRawToken_whenIssuingToken() {
        when(jwtProperties.getRefreshTokenTtl()).thenReturn(REFRESH_TOKEN_TTL);

        String rawToken = sut.issueToken(USER_ID);

        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken stored = tokenCaptor.getValue();

        assertThat(stored.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(stored.getTokenHash()).hasSize(64);
        assertThat(stored.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void shouldReturnStoredToken_whenTokenIsValid() {
        RefreshToken storedToken = activeToken();
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(storedToken));

        RefreshToken result = sut.validateToken(RAW_TOKEN);

        assertThat(result).isSameAs(storedToken);
    }

    @Test
    void shouldThrowInvalidRefreshToken_whenTokenIsUnknown() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.validateToken(RAW_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldThrowInvalidRefreshToken_whenTokenWasRotated() {
        RefreshToken revokedToken = revokedToken(RevocationReason.ROTATED);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> sut.validateToken(RAW_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldThrowInvalidRefreshToken_whenTokenWasRevokedByPasswordChange() {
        RefreshToken revokedToken = revokedToken(RevocationReason.PASSWORD_CHANGED);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> sut.validateToken(RAW_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldThrowInvalidRefreshToken_whenTokenIsExpired() {
        RefreshToken expiredToken = expiredToken();
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> sut.validateToken(RAW_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldPersistRevocation_whenRevokingToken() {
        RefreshToken token = activeToken();

        sut.revokeToken(token, RevocationReason.ROTATED);

        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getRevokedAt()).isNotNull();
        assertThat(tokenCaptor.getValue().getRevokedReason()).isEqualTo(RevocationReason.ROTATED);
    }

    @Test
    void shouldRevokeEveryToken_whenRevokingAllForUser() {
        List<RefreshToken> tokens = List.of(activeToken(), activeToken());
        when(refreshTokenRepository.findByUserIdAndRevokedAtIsNull(USER_ID)).thenReturn(tokens);

        sut.revokeAllTokens(USER_ID, RevocationReason.PASSWORD_CHANGED);

        verify(refreshTokenRepository).saveAll(tokens);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getRevokedAt()).isNotNull();
            assertThat(token.getRevokedReason()).isEqualTo(RevocationReason.PASSWORD_CHANGED);
        });
    }

    private RefreshToken activeToken() {
        return new RefreshToken(USER_ID, TOKEN_HASH, Instant.now().plus(ONE_DAY));
    }

    private RefreshToken expiredToken() {
        return new RefreshToken(USER_ID, TOKEN_HASH, Instant.now().minus(ONE_DAY));
    }

    private RefreshToken revokedToken(RevocationReason reason) {
        RefreshToken token = activeToken();
        token.revoke(Instant.now(), reason);
        return token;
    }
}

