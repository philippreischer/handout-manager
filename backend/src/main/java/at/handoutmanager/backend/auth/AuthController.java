package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.security.RefreshCookieFactory;
import at.handoutmanager.backend.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService,
                          AuthService authService,
                          RefreshCookieFactory refreshCookieFactory,
                          PasswordResetService passwordResetService) {
        this.userService = userService;
        this.authService = authService;
        this.refreshCookieFactory = refreshCookieFactory;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        userService.register(request.email(), request.password(), request.userName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        AuthTokens tokens = authService.login(request.email(), request.password());

        ResponseCookie cookie = refreshCookieFactory.create(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(
            @CookieValue(RefreshCookieFactory.COOKIE_NAME) String rawRefreshToken) {
        AuthTokens tokens = authService.refreshTokens(rawRefreshToken);

        ResponseCookie cookie = refreshCookieFactory.create(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = RefreshCookieFactory.COOKIE_NAME, required = false) String rawRefreshToken) {

        if (rawRefreshToken != null) {
            authService.logout(rawRefreshToken);
        }

        ResponseCookie cookie = refreshCookieFactory.expired();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request){
        passwordResetService.requestReset(request.email());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequestDTO request){
        passwordResetService.confirmReset(request.rawToken(), request.newPassword());

        return ResponseEntity.noContent().build();
    }

}
