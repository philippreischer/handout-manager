package at.handoutmanager.backend.auth;

public record AuthTokens(String accessToken,
                         String refreshToken) {
}
