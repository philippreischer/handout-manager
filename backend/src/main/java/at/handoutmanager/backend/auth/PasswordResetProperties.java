package at.handoutmanager.backend.auth;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "handout-manager.password-reset")
public class PasswordResetProperties {

    /** How long a reset link stays valid. Short on purpose. */
    private Duration tokenTtl = Duration.ofMinutes(30);

    /**
     * Frontend page the mail links to; the raw token is appended as a query
     * parameter. Differs per environment, so it cannot live in the code.
     */
    @NotBlank
    private String resetUrl;

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Duration tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public String getResetUrl() {
        return resetUrl;
    }

    public void setResetUrl(String resetUrl) {
        this.resetUrl = resetUrl;
    }
}
