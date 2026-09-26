package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.common.GlobalExceptionHandler;
import at.handoutmanager.backend.security.*;
import at.handoutmanager.backend.user.EmailAlreadyExistsException;
import at.handoutmanager.backend.user.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class})
class AuthControllerTest {

    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String REFRESH_TOKEN = "refresh-token-must-not-leak";
    private static final String OLD_REFRESH_TOKEN = "old-refresh-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CookieProperties cookieProperties;

    @MockitoBean
    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() {
        // lenient: only the tests that actually build a cookie read this value.
        lenient().when(cookieProperties.getSameSite()).thenReturn("Strict");
    }

    //Registration Tests
    @Test
    void shouldReturn201_whenRegistrationSucceeds() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "max@mustermann.com",
                                  "password": "pass1234",
                                  "userName": "Max"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn409_whenEmailIsAlreadyTaken() throws Exception {
        when(userService.register("max@mustermann.com", "pass1234", "Max"))
                .thenThrow(new EmailAlreadyExistsException("max@mustermann.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "max@mustermann.com",
                              "password": "pass1234",
                              "userName": "Max"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Registration failed."))
                .andExpect(jsonPath("$.detail").value(not(containsString("max@mustermann.com"))));
    }

    @Test
    void shouldReturn400_whenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "max@mustermann.com",
                                  "password": "kurz",
                                  "userName": "Max"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password")
                        .value("Password must be between 8 and 72 characters"));

        verify(userService, never()).register(any(), any(), any());
    }

    //Login Tests
    @Test
    void shouldReturnAccessTokenAndCookie_whenLoginSucceeds() throws Exception {
        when(authService.login("max@mustermann.com", "pass1234"))
                .thenReturn(new AuthTokens(ACCESS_TOKEN, REFRESH_TOKEN));
        when(jwtProperties.getRefreshTokenTtl())
                .thenReturn(Duration.ofDays(14));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "max@mustermann.com",
                              "password": "pass1234"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void shouldNotExposeRefreshTokenInBody_whenLoginSucceeds() throws Exception {
        when(authService.login("max@mustermann.com", "pass1234"))
                .thenReturn(new AuthTokens(ACCESS_TOKEN, REFRESH_TOKEN));
        when(jwtProperties.getRefreshTokenTtl())
                .thenReturn(Duration.ofDays(14));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "max@mustermann.com",
                              "password": "pass1234"
                            }
                            """))
                .andExpect(content().string(not(containsString(REFRESH_TOKEN))));
    }

    @Test
    void shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        when(authService.login("max@mustermann.com", "pass1234"))
                .thenThrow(new InvalidCredentialsException("Login failed"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "max@mustermann.com",
                              "password": "pass1234"
                            }
                            """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    //Refresh Tests
    @Test
    void shouldReturnNewAccessTokenAndCookie_whenRefreshSucceeds() throws Exception {
        when(authService.refreshTokens(OLD_REFRESH_TOKEN))
                .thenReturn(new AuthTokens(ACCESS_TOKEN, REFRESH_TOKEN));
        when(jwtProperties.getRefreshTokenTtl())
                .thenReturn(Duration.ofDays(14));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", OLD_REFRESH_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(content().string(not(containsString(REFRESH_TOKEN))));
    }

    @Test
    void shouldReturn401_whenRefreshTokenIsInvalid() throws Exception {
        when(authService.refreshTokens(OLD_REFRESH_TOKEN))
                .thenThrow(new InvalidRefreshTokenException("Refresh token not found"));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", OLD_REFRESH_TOKEN)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    //Logout Tests
    @Test
    void shouldReturn204AndClearCookie_whenLoggingOut() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refresh_token", OLD_REFRESH_TOKEN)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));

        verify(authService).logout(OLD_REFRESH_TOKEN);
    }

    @Test
    void shouldReturn204_whenLogoutWithoutCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));

        verify(authService, never()).logout(any());
    }

}