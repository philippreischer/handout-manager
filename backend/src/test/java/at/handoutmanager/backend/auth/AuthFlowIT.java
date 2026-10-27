package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthFlowIT extends AbstractIntegrationTest {

    private static final String EMAIL = "max@mustermann.com";
    private static final String PASSWORD = "secret1234";
    private static final String USER_NAME = "Max";


    @Test
    void shouldIssueAnAccessTokenAndARefreshCookie_whenLoggingIn() {
        ResponseEntity<Void> registration = register();

        assertThat(registration.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registration.getHeaders().get(HttpHeaders.SET_COOKIE)).isNull();

        ResponseEntity<TokenResponse> login = login();

        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody().accessToken()).isNotBlank();
        assertThat(login.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("refresh_token=")
                .contains("HttpOnly")
                .contains("Path=/api/auth")
                .contains("SameSite");
    }

    @Test
    void shouldRotateTheRefreshToken_whenRefreshing() {
        register();
        ResponseEntity<TokenResponse> login = login();
        HttpHeaders firstCookie = withRefreshCookie(login);

        ResponseEntity<TokenResponse> refresh = restTemplate.exchange("/api/auth/refresh",
                HttpMethod.POST, new HttpEntity<>(firstCookie), TokenResponse.class);

        assertThat(refresh.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refresh.getBody().accessToken()).isNotBlank();
        assertThat(refresh.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .isNotEqualTo(login.getHeaders().getFirst(HttpHeaders.SET_COOKIE));

        ResponseEntity<String> reuse = restTemplate.exchange("/api/auth/refresh",
                HttpMethod.POST, new HttpEntity<>(firstCookie), String.class);

        assertThat(reuse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectTheRefreshToken_whenLoggedOut() {
        register();
        HttpHeaders cookie = withRefreshCookie(login());

        ResponseEntity<Void> logout = restTemplate.exchange("/api/auth/logout",
                HttpMethod.POST, new HttpEntity<>(cookie), Void.class);

        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> refresh = restTemplate.exchange("/api/auth/refresh",
                HttpMethod.POST, new HttpEntity<>(cookie), String.class);

        assertThat(refresh.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReachTheProfile_onlyWithAnAccessToken() {
        register();
        String accessToken = login().getBody().accessToken();

        ResponseEntity<String> anonymous =
                restTemplate.getForEntity("/api/users/me", String.class);

        assertThat(anonymous.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders bearer = new HttpHeaders();
        bearer.setBearerAuth(accessToken);

        ResponseEntity<String> profile = restTemplate.exchange("/api/users/me",
                HttpMethod.GET, new HttpEntity<>(bearer), String.class);

        assertThat(profile.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profile.getBody()).contains(EMAIL, USER_NAME).doesNotContain("passwordHash");
    }

    private ResponseEntity<Void> register() {
        return restTemplate.postForEntity("/api/auth/register",
                Map.of("email", EMAIL, "password", PASSWORD, "userName", USER_NAME), Void.class);
    }

    private ResponseEntity<TokenResponse> login() {
        return restTemplate.postForEntity("/api/auth/login",
                Map.of("email", EMAIL, "password", PASSWORD), TokenResponse.class);
    }
}
