package at.handoutmanager.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequestDTO(@NotBlank(message = "Email is required")
                                      @Email(message = "Email must be a valid address")
                                      @Size(max = 255, message = "Email must not exceed 255 characters")
                                      String email) {
}
