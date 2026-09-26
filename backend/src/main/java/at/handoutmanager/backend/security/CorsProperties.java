package at.handoutmanager.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "handout-manager.cors")
public class CorsProperties {

    private final List<String> allowedOrigins;

    public CorsProperties(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
}
