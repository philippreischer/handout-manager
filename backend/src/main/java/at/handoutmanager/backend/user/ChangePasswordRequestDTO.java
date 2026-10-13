package at.handoutmanager.backend.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(@NotBlank(message = "Password is required")
                                       String password,
                                       @NotBlank(message = "Password is required")
                                       @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
                                       String newPassword
                                       ) {
}
