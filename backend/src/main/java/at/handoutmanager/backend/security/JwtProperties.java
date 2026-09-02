package at.handoutmanager.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "handout-manager.jwt")
public class JwtProperties {

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
