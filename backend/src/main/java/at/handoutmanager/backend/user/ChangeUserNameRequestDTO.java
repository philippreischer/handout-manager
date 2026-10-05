package at.handoutmanager.backend.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUserNameRequestDTO(@NotBlank(message = "Password is required")
                                       String password,
                                       @NotBlank(message = "Username is required")
                                       @Size(max = 255, message = "Username must not exceed 255 characters")
                                       String userName) {
}
