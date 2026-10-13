package at.handoutmanager.backend.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieFactory {

    public static final String COOKIE_NAME = "refresh_token";

    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public RefreshCookieFactory(CookieProperties cookieProperties, JwtProperties jwtProperties) {
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    public ResponseCookie create(String rawToken) {
        return build(rawToken, jwtProperties.getRefreshTokenTtl());
    }

    public ResponseCookie expired() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }
}
