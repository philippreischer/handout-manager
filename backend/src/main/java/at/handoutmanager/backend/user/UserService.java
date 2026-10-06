package at.handoutmanager.backend.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String normalizeEmail(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }


    public User register(String email, String password, String userName) {
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }
        User user = new User(normalizedEmail, passwordEncoder.encode(password), userName);
        return userRepository.save(user);
    }


    public Optional<User> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return userRepository.findByEmail(normalizedEmail);
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User updateUserName(Long id, String rawPassword, String userName) {
        User user = findUserById(id);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Update username failed: wrong password for user {}", user.getId());
            throw new PasswordMismatchException("Update username");
        }

        user.setUserName(userName);

        return userRepository.save(user);
    }

    public User changePassword(Long id, String rawPassword, String newPassword) {
        User user = findUserById(id);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Update password failed: wrong password for user {}", id);
            throw new PasswordMismatchException("Update password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        return userRepository.save(user);
    }
}
