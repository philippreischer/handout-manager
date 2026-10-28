package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class PasswordResetFlowIT extends AbstractIntegrationTest {

    private static final String EMAIL = "max@mustermann.com";
    private static final String PASSWORD = "secret1234";
    private static final String NEW_PASSWORD = "brandnew1234";
    private static final String USER_NAME = "Max";

    // Mirrors the reset-url property in the base class plus what buildResetUrl appends.
    private static final String LINK_PREFIX = "http://localhost:5173/reset-password?token=";

    @Test
    void shouldAcceptTheNewPasswordAndRejectTheOld_whenRedeemingTheLink() {
        registerAndLogin();

        ResponseEntity<String> confirm = confirmReset(requestResetLink(), NEW_PASSWORD);

        assertThat(confirm.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(login(NEW_PASSWORD).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login(PASSWORD).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRevokeExistingSessions_whenRedeemingTheLink() {
        HttpHeaders oldCookie = withRefreshCookie(registerAndLogin());

        confirmReset(requestResetLink(), NEW_PASSWORD);

        ResponseEntity<String> refresh = restTemplate.exchange("/api/auth/refresh",
                HttpMethod.POST, new HttpEntity<>(oldCookie), String.class);

        assertThat(refresh.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectTheLink_whenRedeemedASecondTime() {
        registerAndLogin();
        String rawToken = requestResetLink();

        confirmReset(rawToken, NEW_PASSWORD);

        assertThat(confirmReset(rawToken, "yetanother1234").getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<String> registerAndLogin() {
        restTemplate.postForEntity("/api/auth/register",
                Map.of("email", EMAIL, "password", PASSWORD, "userName", USER_NAME), Void.class);

        return login(PASSWORD);
    }

    private ResponseEntity<String> login(String password) {
        return restTemplate.postForEntity("/api/auth/login",
                Map.of("email", EMAIL, "password", password), String.class);
    }

    /**
     * Returns the raw token, which exists nowhere else: the database holds only its
     * hash. A plain verify would fail here - the listener runs on the mail pool after
     * the commit, so the call has not happened yet when the response arrives. The
     * timeout is what proves the handover onto another thread works at all.
     */
    private String requestResetLink() {
        ResponseEntity<Void> request = restTemplate.postForEntity(
                "/api/auth/password-reset/request", Map.of("email", EMAIL), Void.class);

        assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender, timeout(2000)).sendPasswordReset(eq(EMAIL), linkCaptor.capture());

        assertThat(linkCaptor.getValue()).startsWith(LINK_PREFIX);

        return linkCaptor.getValue().substring(LINK_PREFIX.length());
    }

    private ResponseEntity<String> confirmReset(String rawToken, String newPassword) {
        return restTemplate.postForEntity("/api/auth/password-reset/confirm",
                Map.of("rawToken", rawToken, "newPassword", newPassword), String.class);
    }
}
