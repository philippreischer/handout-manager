package at.handoutmanager.backend.auth;

import at.handoutmanager.backend.security.JwtService;
import at.handoutmanager.backend.security.RefreshTokenRepository;
import at.handoutmanager.backend.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService,
                       JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }


}
