package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.email.EmailSender;
import at.handoutmanager.backend.security.RefreshTokenService;
import at.handoutmanager.backend.security.RevocationReason;
import at.handoutmanager.backend.security.SecureTokenUtil;
import at.handoutmanager.backend.user.User;
import at.handoutmanager.backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserService userService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final EmailSender emailSender;
    private final PasswordResetProperties passwordResetProperties;

    public PasswordResetService(UserService userService,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                RefreshTokenService refreshTokenService,
                                EmailSender emailSender,
                                PasswordResetProperties passwordResetProperties) {
        this.userService = userService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenService = refreshTokenService;
        this.emailSender = emailSender;
        this.passwordResetProperties = passwordResetProperties;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> user = userService.findByEmail(email);

        if (user.isEmpty()) {
            log.info("Password reset requested for an unknown email");
            return;
        }

        Long userId = user.get().getId();
        invalidateOpenTokens(userId);

        String rawToken = SecureTokenUtil.createRawToken();
        Instant expiresAt = Instant.now().plus(passwordResetProperties.getTokenTtl());
        passwordResetTokenRepository.save(
                new PasswordResetToken(userId, SecureTokenUtil.hash(rawToken), expiresAt));

        log.info("Password reset token issued for user {}", userId);
        emailSender.sendPasswordReset(user.get().getEmail(), buildResetUrl(rawToken));
    }

    @Transactional
    public void confirmReset(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHash(SecureTokenUtil.hash(rawToken))
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Reset token not found"));

        if (token.getUsedAt() != null) {
            throw new InvalidPasswordResetTokenException("Reset token has already been used");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidPasswordResetTokenException("Reset token has expired");
        }

        userService.resetPassword(token.getUserId(), newPassword);

        token.markUsed(Instant.now());
        passwordResetTokenRepository.save(token);

        refreshTokenService.revokeAllTokens(token.getUserId(), RevocationReason.PASSWORD_CHANGED);
        log.info("Password reset completed for user {}", token.getUserId());
    }

    private void invalidateOpenTokens(Long userId) {
        List<PasswordResetToken> openTokens =
                passwordResetTokenRepository.findByUserIdAndUsedAtIsNull(userId);

        Instant now = Instant.now();
        for (PasswordResetToken token : openTokens) {
            token.markUsed(now);
        }

        passwordResetTokenRepository.saveAll(openTokens);
    }

    private String buildResetUrl(String rawToken) {
        return passwordResetProperties.getResetUrl() + "?token=" + rawToken;
    }
}
