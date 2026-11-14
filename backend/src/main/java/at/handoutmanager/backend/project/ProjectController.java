package at.handoutmanager.backend.project;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getProjects(@AuthenticationPrincipal Long userId) {

        List<ProjectResponseDTO> projects = projectService.getProjectsForUser(userId).stream()
                .map(project -> new ProjectResponseDTO(project.id(), project.name(), project.membershipRole()))
                .toList();

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProject(@AuthenticationPrincipal Long userId,
                                                         @PathVariable Long projectId){
        ProjectSummary project = projectService.getProject(userId, projectId);

        return ResponseEntity.ok(new ProjectResponseDTO(project.id(), project.name(), project.membershipRole()));
    }

    @PatchMapping("/{projectId}/name")
    public ResponseEntity<ProjectResponseDTO> renameProject(@AuthenticationPrincipal Long userId,
                                                            @PathVariable Long projectId,
                                                            @Valid @RequestBody ProjectRenameRequestDTO request){
        ProjectSummary project = projectService.renameProject(userId, projectId, request.name());

        return ResponseEntity.ok(new ProjectResponseDTO(project.id(), project.name(), project.membershipRole()));
    }
}
