package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.security.JwtProperties;
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

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final UserService userService;
    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public AuthController(UserService userService,
                          AuthService authService,
                          JwtProperties jwtProperties) {
        this.userService = userService;
        this.authService = authService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        userService.register(request.email(), request.password(), request.userName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        AuthTokens tokens = authService.login(request.email(), request.password());

        ResponseCookie cookie = buildRefreshCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(tokens.accessToken()));

    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(
            @CookieValue(REFRESH_TOKEN_COOKIE) String rawRefreshToken) {
        AuthTokens tokens = authService.refreshTokens(rawRefreshToken);

        ResponseCookie cookie = buildRefreshCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(tokens.accessToken()));
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                // TODO: must be true in production. Profile-dependent - a Secure cookie
                // is rejected by browsers over plain HTTP during local development.
                .secure(false)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(jwtProperties.getRefreshTokenTtl())
                .build();

    }

}
