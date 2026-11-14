package at.handoutmanager.backend.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRenameRequestDTO(@NotBlank(message = "Project name is required")
                                      @Size(max = 255, message = "Project name must not exceed 255 characters")
                                      String name) {
}
