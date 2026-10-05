package at.handoutmanager.backend.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal Long userId) {
        User user = userService.findUserById(userId);

        return ResponseEntity.ok(UserResponseDTO.from(user));
    }

    @PatchMapping("/me/username")
    public ResponseEntity<UserResponseDTO> updateUserName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangeUserNameRequestDTO request) {

        User user = userService.updateUserName(userId, request.password(), request.userName());

        return ResponseEntity.ok(UserResponseDTO.from(user));
    }

    //@PatchMapping("/me")
    //changePassword(){}

    //@DeleteMapping("/me")
    //deleteCurrentUser(){}
}
