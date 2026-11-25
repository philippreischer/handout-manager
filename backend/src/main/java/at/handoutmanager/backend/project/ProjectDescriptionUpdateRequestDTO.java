package at.handoutmanager.backend.project;

import jakarta.validation.constraints.Size;

public record ProjectDescriptionUpdateRequestDTO(@Size(max = 2000, message = "Description must not exceed 2000 characters")
                                                 String description) {
}
