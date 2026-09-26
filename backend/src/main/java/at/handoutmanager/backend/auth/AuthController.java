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

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final UserService userService;
    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    public AuthController(UserService userService,
                          AuthService authService,
                          JwtProperties jwtProperties,
                          CookieProperties cookieProperties) {
        this.userService = userService;
        this.authService = authService;
        this.jwtProperties = jwtProperties;
        this.cookieProperties = cookieProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        userService.register(request.email(), request.password(), request.userName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        AuthTokens tokens = authService.login(request.email(), request.password());

        ResponseCookie cookie = buildRefreshCookie(tokens.refreshToken(), jwtProperties.getRefreshTokenTtl());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(tokens.accessToken()));

    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(
            @CookieValue(REFRESH_TOKEN_COOKIE) String rawRefreshToken) {
        AuthTokens tokens = authService.refreshTokens(rawRefreshToken);

        ResponseCookie cookie = buildRefreshCookie(tokens.refreshToken(), jwtProperties.getRefreshTokenTtl());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String rawRefreshToken) {

        if (rawRefreshToken != null) {
            authService.logout(rawRefreshToken);
        }

        ResponseCookie cookie = buildRefreshCookie("", Duration.ZERO);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();

    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }

}
