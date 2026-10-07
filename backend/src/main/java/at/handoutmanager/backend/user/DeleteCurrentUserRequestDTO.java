package at.handoutmanager.backend.user;

import jakarta.validation.constraints.NotBlank;

public record DeleteCurrentUserRequestDTO(@NotBlank(message = "Password is required")
                                          String password) {
}
