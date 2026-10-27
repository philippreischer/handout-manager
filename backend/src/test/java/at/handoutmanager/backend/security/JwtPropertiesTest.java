package at.handoutmanager.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    // Length is all that matters here; nothing in this test builds a token.
    private static final String VALID_SECRET = "a".repeat(44);
    private static final String ONE_CHAR_SHORT = "a".repeat(43);

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withUserConfiguration(EnableJwtProperties.class);

    @Test
    void shouldBindTheValues_whenTheSecretIsLongEnough() {
        runner.withPropertyValues(
                        "handout-manager.jwt.secret=" + VALID_SECRET,
                        "handout-manager.jwt.access-token-ttl=15m",
                        "handout-manager.jwt.refresh-token-ttl=14d")
                .run(context -> {
                    assertThat(context).hasNotFailed();

                    JwtProperties properties = context.getBean(JwtProperties.class);

                    assertThat(properties.getSecret()).isEqualTo(VALID_SECRET);
                    assertThat(properties.getAccessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
                    assertThat(properties.getRefreshTokenTtl()).isEqualTo(Duration.ofDays(14));
                });
    }

    @Test
    void shouldFailToStart_whenTheSecretIsOneCharacterTooShort() {
        runner.withPropertyValues(
                        "handout-manager.jwt.secret=" + ONE_CHAR_SHORT,
                        "handout-manager.jwt.access-token-ttl=15m",
                        "handout-manager.jwt.refresh-token-ttl=14d")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        // The field name lives in the cause, not in the top-level
                        // message. Asserting the explicit text as well: without a
                        // message on the annotation, Hibernate Validator would
                        // translate it to the caller's locale.
                        .rootCause()
                        .isInstanceOf(BindValidationException.class)
                        .hasMessageContaining("on field 'secret'")
                        .hasMessageContaining("at least 44 characters"));
    }

    @Test
    void shouldFailToStart_whenTheSecretIsMissing() {
        runner.withPropertyValues(
                        "handout-manager.jwt.access-token-ttl=15m",
                        "handout-manager.jwt.refresh-token-ttl=14d")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .rootCause()
                        .isInstanceOf(BindValidationException.class)
                        .hasMessageContaining("JWT secret is required"));
    }

    @EnableConfigurationProperties(JwtProperties.class)
    static class EnableJwtProperties {
    }
}
