package at.handoutmanager.backend.auth;

import java.util.Objects;

public record PasswordResetRequestedEvent(String recipient, String resetUrl) {

    public PasswordResetRequestedEvent {
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(resetUrl, "resetUrl must not be null");
    }

    @Override
    public String toString() {
        return "PasswordResetRequestedEvent[recipient=" + recipient + ", resetUrl=***]";
    }
}
