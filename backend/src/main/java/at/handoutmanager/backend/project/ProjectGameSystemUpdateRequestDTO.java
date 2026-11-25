package at.handoutmanager.backend.project;

import jakarta.validation.constraints.Size;

public record ProjectGameSystemUpdateRequestDTO(@Size(max = 255, message = "Game system must not exceed 255 characters")
                                         String gameSystem) {
}
