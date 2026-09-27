package at.handoutmanager.backend.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "max@mustermann.com";
    private static final String UNNORMALIZED_EMAIL = " MAX@Mustermann.COM ";
    private static final String PASSWORD = "pass1234";
    private static final String PASSWORD_HASH = "hashed-pass";
    private static final String USER_NAME = "Max";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService sut;

    @Captor
    private ArgumentCaptor<User> userCaptor;


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

}