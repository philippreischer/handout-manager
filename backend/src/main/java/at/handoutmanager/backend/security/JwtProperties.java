package at.handoutmanager.backend.security;

import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "handout-manager.jwt")
public class JwtProperties {

    @Size(min = 44)
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
