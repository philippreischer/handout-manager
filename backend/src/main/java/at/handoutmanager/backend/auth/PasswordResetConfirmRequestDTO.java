package at.handoutmanager.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequestDTO(@NotBlank(message = "Token is required")
                                             String rawToken,
                                             @NotBlank(message = "Password is required")
                                             @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
                                             String newPassword) {
}
