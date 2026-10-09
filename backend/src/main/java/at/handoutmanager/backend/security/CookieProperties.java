package at.handoutmanager.backend.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "handout-manager.cookie")
public class CookieProperties {

    private final boolean secure;
    private final String sameSite;

    public CookieProperties(boolean secure, String sameSite) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getSameSite() {
        return sameSite;
    }
}
