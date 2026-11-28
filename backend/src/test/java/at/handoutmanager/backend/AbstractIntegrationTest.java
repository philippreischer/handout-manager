package at.handoutmanager.backend;

import at.handoutmanager.backend.email.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

/**
 * Shared setup for every *IT. The annotations belong here and nowhere else:
 * Spring caches one context per distinct test configuration, and each context
 * brings its own database container. One extra property or bean override on a
 * subclass doubles the start-up cost of the whole suite. Plain helper methods
 * do not affect the cache and may be added freely.
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

    protected static final String TEST_PASSWORD = "secret1234";

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Mocked here rather than in the one test that needs it: a bean override changes
     * the context cache key, so a subclass declaring it would start a second context
     * and a second container. Side effect worth having - no test ever tries to reach
     * an SMTP server.
     */
    @MockitoBean
    protected EmailSender emailSender;

    /**
     * Emptied before each test, not after: a failed test leaves its rows behind for
     * inspection. A test-level {@code @Transactional} would not work here - with
     * RANDOM_PORT the server runs on its own connection, outside the test's
     * transaction. CASCADE only reaches tables with a foreign key to a listed table,
     * so every root table must be named: project has no foreign key to user_account.
     */
    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE user_account, project CASCADE");
    }

    // Only the wire shape the tests need, deliberately not the production DTO: a
    // test that deserialises with the same class the server serialises with would
    // not notice a renamed JSON field.
    public record TokenResponse(String accessToken) {}

    public record ProjectResponse(Long id, String name, String gameSystem, String description,
                                  String membershipRole) {}

    protected String registerAndLogin(String email, String userName) {
        restTemplate.postForEntity("/api/auth/register",
                Map.of("email", email, "password", TEST_PASSWORD, "userName", userName), Void.class);

        return restTemplate.postForEntity("/api/auth/login",
                        Map.of("email", email, "password", TEST_PASSWORD), TokenResponse.class)
                .getBody().accessToken();
    }

    protected HttpHeaders bearer(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected Long createProject(String accessToken, String name) {
        return restTemplate.postForEntity("/api/projects",
                        new HttpEntity<>(Map.of("name", name), bearer(accessToken)), ProjectResponse.class)
                .getBody().id();
    }

    protected Long userIdOf(String email) {
        return jdbcTemplate.queryForObject("SELECT id FROM user_account WHERE email = ?", Long.class, email);
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
}