package at.handoutmanager.backend.project;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }


    @PostMapping()
    public ResponseEntity<ProjectResponseDTO> createProject(@AuthenticationPrincipal Long userId,
                                                            @Valid @RequestBody ProjectCreateRequestDTO request){

        ProjectSummary project = projectService.createProject(userId, request.name(), request.nickName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProjectResponseDTO(project.id(), project.name(), project.membershipRole()));
    }
}
