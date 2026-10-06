package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.security.InvalidRefreshTokenException;
import at.handoutmanager.backend.security.JwtService;
import at.handoutmanager.backend.security.RefreshToken;
import at.handoutmanager.backend.security.RefreshTokenService;
import at.handoutmanager.backend.user.User;
import at.handoutmanager.backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthTokens login(String email, String rawPassword){
        User user = userService.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: no account for email {}", email);
                    return new InvalidCredentialsException("Login failed");
                });

        if(!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Login failed: wrong password for user {}", user.getId());
            throw new InvalidCredentialsException("Login failed");
        }

        String accessToken = jwtService.generateAccessToken(user.getId());
        String refreshToken = refreshTokenService.issueToken(user.getId());

        log.info("Login successful for user {}", user.getId());
        return new AuthTokens(accessToken, refreshToken);
    }

    @Transactional
    public AuthTokens refreshTokens(String rawRefreshToken){

        RefreshToken refreshToken = refreshTokenService.validateToken(rawRefreshToken);
        refreshTokenService.revokeToken(refreshToken);
        Long userId = refreshToken.getUserId();

        String accessToken = jwtService.generateAccessToken(userId);
        String newRefreshToken = refreshTokenService.issueToken(userId);

        return new AuthTokens(accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken){
        try {
            RefreshToken refreshToken = refreshTokenService.validateToken(rawRefreshToken);
            refreshTokenService.revokeToken(refreshToken);
        } catch (InvalidRefreshTokenException e) {
            log.debug("Logout with invalid or already revoked token - treated as success");
        }
    }

    @Transactional
    public AuthTokens changePassword(Long userId, String rawPassword, String newPassword) {
        userService.changePassword(userId, rawPassword, newPassword);

        refreshTokenService.revokeAllTokens(userId);

        String accessToken = jwtService.generateAccessToken(userId);
        String refreshToken = refreshTokenService.issueToken(userId);

        log.info("Password changed for user {}", userId);

        return new AuthTokens(accessToken, refreshToken);
    }
}
