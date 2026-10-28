package at.handoutmanager.backend.user;

import at.handoutmanager.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDeletionIT extends AbstractIntegrationTest {

    private static final String EMAIL = "max@mustermann.com";
    private static final String PASSWORD = "secret1234";
    private static final String USER_NAME = "Max";

    @Test
    void shouldRemoveEveryRowOfTheUser_whenDeletingTheAccount() {
        restTemplate.postForEntity("/api/auth/register",
                Map.of("email", EMAIL, "password", PASSWORD, "userName", USER_NAME), Void.class);

        ResponseEntity<TokenResponse> login = restTemplate.postForEntity("/api/auth/login",
                Map.of("email", EMAIL, "password", PASSWORD), TokenResponse.class);

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_account WHERE email = ?", Long.class, EMAIL);

        // Written directly, not through the endpoint: the subject here is the foreign
        // key, not the reset flow, which has an integration test of its own. A row is
        // a row whoever wrote it.
        jdbcTemplate.update("""
                INSERT INTO password_reset_token (user_id, token_hash, expires_at)
                VALUES (?, ?, ?)
                """, userId, "a".repeat(64), OffsetDateTime.now().plusMinutes(30));

        // Without this the test would also pass on an empty database.
        assertThat(countFor("refresh_token", userId)).isOne();
        assertThat(countFor("password_reset_token", userId)).isOne();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getBody().accessToken());

        ResponseEntity<Void> deletion = restTemplate.exchange("/api/users/me", HttpMethod.DELETE,
                new HttpEntity<>(Map.of("password", PASSWORD), headers), Void.class);

        assertThat(deletion.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(countFor("refresh_token", userId)).isZero();
        assertThat(countFor("password_reset_token", userId)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM user_account WHERE id = ?",
                Integer.class, userId)).isZero();
    }

    private Integer countFor(String table, Long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM " + table + " WHERE user_id = ?", Integer.class, userId);
    }
}
