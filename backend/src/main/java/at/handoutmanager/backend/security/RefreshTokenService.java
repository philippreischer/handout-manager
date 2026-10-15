package at.handoutmanager.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    public String issueToken(Long userId) {
        String rawToken = SecureTokenUtil.createRawToken();

        Instant expiresAt = Instant.now().plus(jwtProperties.getRefreshTokenTtl());
        refreshTokenRepository.save(new RefreshToken(userId, SecureTokenUtil.hash(rawToken), expiresAt));

        return rawToken;
    }

    public RefreshToken validateToken(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(SecureTokenUtil.hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (token.getRevokedAt() != null) {
            if (token.getRevokedReason() == RevocationReason.ROTATED) {
                log.warn("Rotated refresh token presented again for user {} - possible token theft",
                        token.getUserId());
            } else {
                log.info("Revoked refresh token presented for user {} (reason: {})",
                        token.getUserId(), token.getRevokedReason());
            }

            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        return token;
    }

    public void revokeToken(RefreshToken token, RevocationReason reason) {
        token.revoke(Instant.now(), reason);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAllTokens(Long userId, RevocationReason reason) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId);

        Instant now = Instant.now();
        for (RefreshToken token : tokens) {
            token.revoke(now, reason);
        }

        refreshTokenRepository.saveAll(tokens);
        log.info("Revoked {} refresh tokens for user {} (reason: {})", tokens.size(), userId, reason);
    }
}
