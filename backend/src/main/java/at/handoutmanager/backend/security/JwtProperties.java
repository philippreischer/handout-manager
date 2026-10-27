package at.handoutmanager.backend.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "handout-manager.jwt")
public class JwtProperties {

    // Both are needed: @Size alone passes on a null value, because Jakarta
    // Validation only checks a size when there is something to measure. The
    // explicit messages keep the text out of the caller's locale.
    @NotBlank(message = "JWT secret is required")
    @Size(min = 44, message = "JWT secret must be at least 44 characters")
    private final String secret;

    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {
        this.secret = secret;
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public String getSecret() {
        return secret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }
}
