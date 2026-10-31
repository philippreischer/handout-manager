package at.handoutmanager.backend.user;

import at.handoutmanager.backend.auth.AuthService;
import at.handoutmanager.backend.auth.AuthTokens;
import at.handoutmanager.backend.common.GlobalExceptionHandler;
import at.handoutmanager.backend.security.CookieProperties;
import at.handoutmanager.backend.security.CorsProperties;
import at.handoutmanager.backend.security.JwtAuthenticationFilter;
import at.handoutmanager.backend.security.JwtProperties;
import at.handoutmanager.backend.security.JwtService;
import at.handoutmanager.backend.security.RefreshCookieFactory;
import at.handoutmanager.backend.security.RestAuthenticationEntryPoint;
import at.handoutmanager.backend.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class,
        RefreshCookieFactory.class})
class UserControllerTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "max@mustermann.com";
    private static final String USER_NAME = "Max";
    private static final String NEW_USER_NAME = "Moritz";
    private static final String PASSWORD_HASH = "$2a$10$must-never-reach-the-client";
    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:00:00Z");

    private static final String PASSWORD = "pass1234";
    private static final String WRONG_PASSWORD = "wrong-password";
    private static final String NEW_PASSWORD = "newpass1234";

    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String REFRESH_TOKEN = "refresh-token-must-not-leak";

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

    // Current user

    @Test
    void shouldReturnCurrentUser_whenAuthenticated() throws Exception {
        when(userService.findUserById(USER_ID)).thenReturn(user(USER_NAME));

        mockMvc.perform(asCurrentUser(get("/api/users/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.userName").value(USER_NAME))
                .andExpect(jsonPath("$.createdAt").value("2026-01-15T10:00:00Z"));
    }

    @Test
    void shouldNotExposePasswordHash_whenReturningCurrentUser() throws Exception {
        when(userService.findUserById(USER_ID)).thenReturn(user(USER_NAME));

        mockMvc.perform(asCurrentUser(get("/api/users/me")))
                .andExpect(content().string(not(containsString(PASSWORD_HASH))));
    }

    // Username

    @Test
    void shouldReturnUpdatedUser_whenUserNameChangeSucceeds() throws Exception {
        when(userService.updateUserName(USER_ID, PASSWORD, NEW_USER_NAME))
                .thenReturn(user(NEW_USER_NAME));

        mockMvc.perform(asCurrentUser(patch("/api/users/me/username"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "pass1234",
                                  "userName": "Moritz"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value(NEW_USER_NAME));
    }

    @Test
    void shouldReturn403_whenPasswordIsWrongOnUserNameChange() throws Exception {
        when(userService.updateUserName(USER_ID, WRONG_PASSWORD, NEW_USER_NAME))
                .thenThrow(new PasswordMismatchException("Update username"));

        mockMvc.perform(asCurrentUser(patch("/api/users/me/username"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "wrong-password",
                                  "userName": "Moritz"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Current password is incorrect."));
    }

    @Test
    void shouldReturn400_whenUserNameIsBlank() throws Exception {
        mockMvc.perform(asCurrentUser(patch("/api/users/me/username"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "pass1234",
                                  "userName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.userName").value("Username is required"));

        verify(userService, never()).updateUserName(any(), any(), any());
    }

    @Test
    void shouldReturn400_whenPasswordIsBlankOnUserNameChange() throws Exception {
        mockMvc.perform(asCurrentUser(patch("/api/users/me/username"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "",
                                  "userName": "Moritz"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));

        // A missing password must not reach the service: a wrong one answers 403,
        // an absent one belongs to validation and answers 400.
        verify(userService, never()).updateUserName(any(), any(), any());
    }

    // Password

    @Test
    void shouldReturnNewAccessTokenAndCookie_whenPasswordChangeSucceeds() throws Exception {
        when(authService.changePassword(USER_ID, PASSWORD, NEW_PASSWORD))
                .thenReturn(new AuthTokens(ACCESS_TOKEN, REFRESH_TOKEN));
        when(jwtProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(14));

        mockMvc.perform(asCurrentUser(patch("/api/users/me/password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "pass1234",
                                  "newPassword": "newpass1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(content().string(not(containsString(REFRESH_TOKEN))));
    }

    @Test
    void shouldReturn403_whenPasswordIsWrongOnPasswordChange() throws Exception {
        when(authService.changePassword(USER_ID, WRONG_PASSWORD, NEW_PASSWORD))
                .thenThrow(new PasswordMismatchException("Change password"));

        mockMvc.perform(asCurrentUser(patch("/api/users/me/password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "wrong-password",
                                  "newPassword": "newpass1234"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldReturn400_whenNewPasswordIsTooShort() throws Exception {
        mockMvc.perform(asCurrentUser(patch("/api/users/me/password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "pass1234",
                                  "newPassword": "kurz"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword")
                        .value("Password must be between 8 and 72 characters"));

        verify(authService, never()).changePassword(any(), any(), any());
    }

    @Test
    void shouldReturn400_whenCurrentPasswordIsBlankOnPasswordChange() throws Exception {
        mockMvc.perform(asCurrentUser(patch("/api/users/me/password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "",
                                  "newPassword": "newpass1234"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));

        verify(authService, never()).changePassword(any(), any(), any());
    }

    // Account deletion

    @Test
    void shouldReturn204AndClearCookie_whenAccountIsDeleted() throws Exception {
        mockMvc.perform(asCurrentUser(delete("/api/users/me"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "pass1234"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));

        verify(userService).deleteCurrentUser(USER_ID, PASSWORD);
    }

    @Test
    void shouldReturn403_whenPasswordIsWrongOnAccountDeletion() throws Exception {
        doThrow(new PasswordMismatchException("Delete current user"))
                .when(userService).deleteCurrentUser(USER_ID, WRONG_PASSWORD);

        mockMvc.perform(asCurrentUser(delete("/api/users/me"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldReturn400_whenPasswordIsBlankOnAccountDeletion() throws Exception {
        mockMvc.perform(asCurrentUser(delete("/api/users/me"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));

        verify(userService, never()).deleteCurrentUser(any(), any());
    }


    private MockHttpServletRequestBuilder asCurrentUser(MockHttpServletRequestBuilder request) {
        return request.with(authentication(
                new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())));
    }

    private User user(String userName) {
        User user = new User(EMAIL, PASSWORD_HASH, userName);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        ReflectionTestUtils.setField(user, "createdAt", CREATED_AT);
        return user;
    }
}
