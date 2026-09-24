package at.handoutmanager.backend.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: temporary endpoint to verify the JWT filter.
// Replace with a proper /api/users/me once the user feature exists.
@RestController
public class MeController {

    @GetMapping("/api/me")
    public ResponseEntity<Long> me() {
        Long userId = (Long) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ResponseEntity.ok(userId);
    }
}
