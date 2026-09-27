package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.security.InvalidRefreshTokenException;
import at.handoutmanager.backend.security.JwtService;
import at.handoutmanager.backend.security.RefreshToken;
import at.handoutmanager.backend.security.RefreshTokenService;
import at.handoutmanager.backend.user.User;
import at.handoutmanager.backend.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "max@mustermann.com";
    private static final String UNKNOWN_EMAIL = "unknown@example.com";
    private static final String PASSWORD = "pass1234";
    private static final String WRONG_PASSWORD = "wrong-password";
    private static final String PASSWORD_HASH = "hashed-pass";
    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String REFRESH_TOKEN = "refresh-token-must-not-leak";
    private static final String OLD_REFRESH_TOKEN = "old-refresh-token-must-not-leak";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token-must-not-leak";

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService sut;

    //Login
    @Test
    void shouldReturnTokens_whenCredentialsAreValid(){
        when(userService.findByEmail(EMAIL))
                .thenReturn(Optional.of(userWithId(1L)));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH))
                .thenReturn(true);
        when(jwtService.generateAccessToken(1L))
                .thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.issueToken(1L))
                .thenReturn(REFRESH_TOKEN);

        AuthTokens result = sut.login(EMAIL, PASSWORD);

        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void shouldThrowInvalidCredentials_whenEmailIsUnknown(){
        when(userService.findByEmail(UNKNOWN_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.login(UNKNOWN_EMAIL, PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);

    }

    @Test
    void shouldThrowInvalidCredentials_whenPasswordIsWrong(){
        when(userService.findByEmail(EMAIL))
                .thenReturn(Optional.of(userWithId(1L)));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH))
                .thenReturn(false);

        assertThatThrownBy(() -> sut.login(EMAIL, WRONG_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldNotIssueTokens_whenPasswordIsWrong(){
        when(userService.findByEmail(EMAIL))
                .thenReturn(Optional.of(userWithId(1L)));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH))
                .thenReturn(false);

        assertThatThrownBy(() -> sut.login(EMAIL, WRONG_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).issueToken(any());
    }

    //RefreshTokens
    @Test
    void shouldReturnNewTokens_whenRefreshTokenIsValid(){
        when(refreshTokenService.validateToken(OLD_REFRESH_TOKEN)).thenReturn(oldToken());
        when(jwtService.generateAccessToken(1L)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.issueToken(1L)).thenReturn(NEW_REFRESH_TOKEN);

        AuthTokens result = sut.refreshTokens(OLD_REFRESH_TOKEN);

        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
    }

    @Test
    void shouldRevokeOldToken_whenRefreshing(){
        RefreshToken oldToken = oldToken();
        when(refreshTokenService.validateToken(OLD_REFRESH_TOKEN)).thenReturn(oldToken);
        when(jwtService.generateAccessToken(1L)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.issueToken(1L)).thenReturn(NEW_REFRESH_TOKEN);

        sut.refreshTokens(OLD_REFRESH_TOKEN);

        verify(refreshTokenService).revokeToken(oldToken);
    }

    @Test
    void shouldPropagateException_whenRefreshTokenIsInvalid(){
        when(refreshTokenService.validateToken(OLD_REFRESH_TOKEN))
                .thenThrow(new InvalidRefreshTokenException("Refresh token not found"));

        assertThatThrownBy(() -> sut.refreshTokens(OLD_REFRESH_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenService, never()).issueToken(any());
    }

    //Logout
    @Test
    void shouldRevokeToken_whenLoggingOut(){
        RefreshToken oldToken = oldToken();
        when(refreshTokenService.validateToken(OLD_REFRESH_TOKEN)).thenReturn(oldToken);

        sut.logout(OLD_REFRESH_TOKEN);

        verify(refreshTokenService).revokeToken(oldToken);
    }

    @Test
    void shouldNotThrow_whenLogoutTokenIsInvalid(){
        when(refreshTokenService.validateToken(OLD_REFRESH_TOKEN))
                .thenThrow(new InvalidRefreshTokenException("Refresh token not found"));

        assertThatCode(() -> sut.logout(OLD_REFRESH_TOKEN))
                .doesNotThrowAnyException();
    }

    //Helper
    private User userWithId(Long id) {
        User user = new User(EMAIL, PASSWORD_HASH, "Max");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private RefreshToken oldToken() {
        return new RefreshToken(
                1L, "old-hash", Instant.now().plus(Duration.ofDays(14))
        );
    }

}
