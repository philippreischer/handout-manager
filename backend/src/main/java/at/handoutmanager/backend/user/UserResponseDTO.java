package at.handoutmanager.backend.user;
import java.time.Instant;

public record UserResponseDTO(Long id,
                              String email,
                              String userName,
                              Instant createdAt
                              ) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getUserName(),
                user.getCreatedAt());
    }
}
