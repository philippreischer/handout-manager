package at.handoutmanager.backend.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequestDTO(@NotBlank(message = "Project name is required")
                                      @Size(max = 255, message = "Project name must not exceed 255 characters")
                                      String name,
                                      @Size(max = 255, message = "Nickname must not exceed 255 characters")
                                      String nickName) {
}
