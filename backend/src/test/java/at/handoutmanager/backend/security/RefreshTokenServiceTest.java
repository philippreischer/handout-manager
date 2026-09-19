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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenService sut;

    @Captor
    private ArgumentCaptor<RefreshToken> tokenCaptor;

    @Test
    void shouldStoreHashInsteadOfRawToken_whenIssuingToken(){
        when(jwtProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(14));

        String rawToken = sut.issueToken(42L);

        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken stored = tokenCaptor.getValue();

        assertThat(stored.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(stored.getTokenHash()).hasSize(64);
        assertThat(stored.getUserId()).isEqualTo(42L);
    }

    @Test
    void shouldReturnStoredToken_whenTokenIsValid(){
        RefreshToken storedToken = tokenWith(Instant.now().plus(Duration.ofDays(1)), null);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(storedToken));

        RefreshToken result = sut.validateToken("some-raw-value");

        assertThat(result).isSameAs(storedToken);
    }

    @Test
    void shouldThrowInvalidRefreshToken_whenTokenIsUnknown(){
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.validateToken("some-raw-value"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldThrowInvalidRefreshToken_whenTokenIsRevoked(){
        RefreshToken revokedToken = tokenWith(Instant.now().plus(Duration.ofDays(1)), Instant.now());
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> sut.validateToken("some-raw-value"))
                .isInstanceOf(InvalidRefreshTokenException.class);

    }

    @Test
    void shouldThrowInvalidRefreshToken_whenTokenIsExpired(){
        RefreshToken expiredToken = tokenWith(Instant.now().minus(Duration.ofDays(1)), null);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> sut.validateToken("some-raw-value"))
                .isInstanceOf(InvalidRefreshTokenException.class);

    }

    @Test
    void shouldPersistRevocation_whenRevokingToken(){
        RefreshToken token = tokenWith(Instant.now().plus(Duration.ofDays(1)), null);

        sut.revokeToken(token);

        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getRevokedAt()).isNotNull();
    }

    private RefreshToken tokenWith(Instant expiresAt, Instant revokedAt){
        RefreshToken token = new RefreshToken(42L, "some-hash", expiresAt);
        if (revokedAt != null) {
            token.setRevokedAt(revokedAt);
        }
        return token;
    }
}

