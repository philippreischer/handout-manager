package at.handoutmanager.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.time.Duration;

class JwtServiceTest {

    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQtZm9yLWp3dC1zZXJ2aWNlLXVuaXQtdGVzdHMtMzJieXRlcys=";
    private static final String OTHER_SECRET = "b3RoZXItc2VjcmV0LWZvci1qd3Qtc2VydmljZS10ZXN0cy0zMmJ5dGVzKysrKw==";
    private JwtService sut;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                TEST_SECRET,
                Duration.ofMinutes(15),
                Duration.ofDays(14));
        sut = new JwtService(properties);
    }

    @Test
    void generatedTokenContainsUserId() {
        Long userId = 42L;
        String token = sut.generateAccessToken(userId);

        assertThat(sut.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        JwtProperties otherProperties = new JwtProperties(
                OTHER_SECRET,
                Duration.ofMinutes(15),
                Duration.ofDays(14));
        JwtService otherService = new JwtService(otherProperties);
        String foreignToken = otherService.generateAccessToken(42L);

        assertThatThrownBy(() -> sut.extractUserId(foreignToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void tamperedTokenIsRejected() {
        Long userId = 42L;
        String token = sut.generateAccessToken(userId);

        char lastChar = token.charAt(token.length() - 1);
        String changedToken = token.substring(0, token.length() - 1) + (lastChar != 'A'? 'A': 'B');

        assertThatThrownBy(() -> sut.extractUserId(changedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtProperties expiredProperties = new JwtProperties(
                TEST_SECRET,
                Duration.ofMinutes(-1),
                Duration.ofDays(14));
        JwtService expiredService = new JwtService(expiredProperties);
        String expiredToken = expiredService.generateAccessToken(42L);

        assertThatThrownBy(() -> sut.extractUserId(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
