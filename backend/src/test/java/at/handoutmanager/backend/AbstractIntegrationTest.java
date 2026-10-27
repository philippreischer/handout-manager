package at.handoutmanager.backend;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Shared setup for every *IT. The annotations belong here and nowhere else:
 * Spring caches one context per distinct test configuration, and each context
 * brings its own database container. One extra property on a subclass doubles
 * the start-up cost of the whole suite.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.mail.host=localhost",
                "handout-manager.password-reset.reset-url=http://localhost:5173/reset-password"
        })
// Spring Boot 4 no longer registers the TestRestTemplate bean on its own.
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Emptied before each test, not after: a failed test leaves its rows behind for
     * inspection. A test-level {@code @Transactional} would not work here - with
     * RANDOM_PORT the server runs on its own connection, outside the test's
     * transaction. CASCADE from user_account reaches every table with a foreign key
     * to it, so this stays correct as the schema grows.
     */
    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE user_account CASCADE");
    }

    protected HttpHeaders withRefreshCookie(ResponseEntity<?> response) {
        HttpHeaders headers = new HttpHeaders();
        List<String> setCookie = response.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (setCookie != null) {
            for (String value : setCookie) {
                // Only name=value belongs in a Cookie header; everything after the
                // first semicolon is a Set-Cookie attribute.
                headers.add(HttpHeaders.COOKIE, value.split(";", 2)[0]);
            }
        }

        return headers;
    }

    // Only the wire shape the tests need, deliberately not the production DTO: a
    // test that deserialises with the same class the server serialises with would
    // not notice a renamed JSON field.
    public record TokenResponse(String accessToken) {}
}
