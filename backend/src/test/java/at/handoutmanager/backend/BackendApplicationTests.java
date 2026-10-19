package at.handoutmanager.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Mail host and reset URL deliberately have no defaults in the application
// configuration, so a missing value stops the application from starting. This
// test supplies both; no mail is ever sent here.
@SpringBootTest(properties = {
		"spring.mail.host=localhost",
		"handout-manager.password-reset.reset-url=http://localhost:5173/reset-password"
})
@Import(TestcontainersConfiguration.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}
}
