package at.handoutmanager.backend.user;

import at.handoutmanager.backend.auth.AuthService;
import at.handoutmanager.backend.auth.AuthTokens;
import at.handoutmanager.backend.auth.TokenResponseDTO;
import at.handoutmanager.backend.security.RefreshCookieFactory;
import at.handoutmanager.backend.security.RefreshToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;

    public UserController(UserService userService,
                          AuthService authService,
                          RefreshCookieFactory refreshCookieFactory) {
        this.userService = userService;
        this.authService = authService;
        this.refreshCookieFactory = refreshCookieFactory;
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

    @PatchMapping("/me/password")
    public ResponseEntity<TokenResponseDTO> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequestDTO request) {

        AuthTokens tokens = authService.changePassword(
                userId, request.password(), request.newPassword());

        ResponseCookie refreshCookie = refreshCookieFactory.create(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new TokenResponseDTO(tokens.accessToken()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DeleteCurrentUserRequestDTO request){

        userService.deleteCurrentUser(userId, request.password());

        ResponseCookie cookie = refreshCookieFactory.expired();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
