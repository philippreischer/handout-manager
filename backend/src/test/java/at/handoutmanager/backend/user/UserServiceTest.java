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

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;


    @Test
    void shouldReturnCreatedUser_whenEmailIsAvailable() {
        when(userRepository.existsByEmail("max@mustermann.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed-pass");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register("max@mustermann.com", "pass123", "Max");

        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();

        assertThat(captured.getEmail()).isEqualTo("max@mustermann.com");
        assertThat(captured.getPasswordHash()).isEqualTo("hashed-pass");
        assertThat(captured.getUserName()).isEqualTo("Max");
        assertThat(result).isSameAs(captured);
    }
    @Test
    void shouldThrowEmailAlreadyExists_whenEmailIsTaken(){
        when(userRepository.existsByEmail("max@mustermann.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("max@mustermann.com", "pass123", "Max"))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNormalizeEmail_whenRegistering(){
        when(userRepository.existsByEmail("max@mustermann.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed-pass");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(" MAX@Mustermann.COM ", "pass123", "Max");

        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();

        assertThat(captured.getEmail()).isEqualTo("max@mustermann.com");
    }

    @Test
    void shouldNormalizeEmail_whenLookingUpByEmail() {
        User existingUser = new User("max@mustermann.com", "hashed-pass", "Max");
        when(userRepository.findByEmail("max@mustermann.com"))
                .thenReturn(Optional.of(existingUser));

        Optional<User> result = userService.findByEmail(" MAX@Mustermann.COM ");

        assertThat(result).contains(existingUser);
    }

}