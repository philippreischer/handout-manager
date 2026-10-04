package at.handoutmanager.backend.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
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

}
