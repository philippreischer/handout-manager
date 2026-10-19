package at.handoutmanager.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// The mail host deliberately has no default in the application configuration, so
// the application fails to start when it is missing. This test supplies one; no
// mail is ever sent here.
@SpringBootTest(properties = "spring.mail.host=localhost")
@Import(TestcontainersConfiguration.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}
}
