package at.handoutmanager.backend.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "max@mustermann.com";
    private static final String UNNORMALIZED_EMAIL = " MAX@Mustermann.COM ";
    private static final String PASSWORD = "pass1234";
    private static final String PASSWORD_HASH = "hashed-pass";
    private static final String WRONG_PASSWORD = "wrong-password";
    private static final String NEW_PASSWORD = "newpass1234";
    private static final String NEW_PASSWORD_HASH = "hashed-newpass";
    private static final String USER_NAME = "Max";
    private static final String NEW_USER_NAME = "Moritz";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService sut;

    @Captor
    private ArgumentCaptor<User> userCaptor;


    // Registration

    @Test
    void shouldReturnCreatedUser_whenEmailIsAvailable() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = sut.register(EMAIL, PASSWORD, USER_NAME);

        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();

        assertThat(captured.getEmail()).isEqualTo(EMAIL);
        assertThat(captured.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(captured.getUserName()).isEqualTo(USER_NAME);
        assertThat(result).isSameAs(captured);
    }

    @Test
    void shouldThrowEmailAlreadyExists_whenEmailIsTaken() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> sut.register(EMAIL, PASSWORD, USER_NAME))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNormalizeEmail_whenRegistering() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        sut.register(UNNORMALIZED_EMAIL, PASSWORD, USER_NAME);

        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();

        assertThat(captured.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void shouldNormalizeEmail_whenLookingUpByEmail() {
        User existingUser = new User(EMAIL, PASSWORD_HASH, USER_NAME);
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(existingUser));

        Optional<User> result = sut.findByEmail(UNNORMALIZED_EMAIL);

        assertThat(result).contains(existingUser);
    }

    // Lookup by id

    @Test
    void shouldThrowUserNotFound_whenIdIsUnknown() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.findUserById(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    // Username

    @Test
    void shouldSaveNewUserName_whenPasswordIsCorrect() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        sut.updateUserName(USER_ID, PASSWORD, NEW_USER_NAME);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserName()).isEqualTo(NEW_USER_NAME);
    }

    @Test
    void shouldThrowPasswordMismatch_whenUpdatingUserNameWithWrongPassword() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> sut.updateUserName(USER_ID, WRONG_PASSWORD, NEW_USER_NAME))
                .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, never()).save(any());
    }

    // Password

    @Test
    void shouldStoreHashedNewPassword_whenChangingPassword() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_PASSWORD_HASH);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        sut.changePassword(USER_ID, PASSWORD, NEW_PASSWORD);

        // The encoded value must reach the entity - never the plain text.
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(NEW_PASSWORD_HASH);
    }

    @Test
    void shouldThrowPasswordMismatch_whenChangingPasswordWithWrongPassword() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> sut.changePassword(USER_ID, WRONG_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // Account deletion

    @Test
    void shouldDeleteUser_whenPasswordIsCorrect() {
        User user = existingUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

        sut.deleteCurrentUser(USER_ID, PASSWORD);

        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowPasswordMismatch_whenDeletingWithWrongPassword() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> sut.deleteCurrentUser(USER_ID, WRONG_PASSWORD))
                .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, never()).delete(any());
    }

    private User existingUser() {
        User user = new User(EMAIL, PASSWORD_HASH, USER_NAME);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }
}