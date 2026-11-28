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

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@AuthenticationPrincipal Long userId,
                                                            @Valid @RequestBody ProjectCreateRequestDTO request){
        ProjectSummary project = projectService.createProject(userId, request.name(), request.gameSystem(), request.description(), request.nickName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProjectResponseDTO.from(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getProjects(@AuthenticationPrincipal Long userId) {
        List<ProjectResponseDTO> projects = projectService.getProjectsForUser(userId).stream()
                .map(ProjectResponseDTO::from)
                .toList();

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProject(@AuthenticationPrincipal Long userId,
                                                         @PathVariable Long projectId){
        ProjectSummary project = projectService.getProject(userId, projectId);

        return ResponseEntity.ok(ProjectResponseDTO.from(project));
    }

    @PatchMapping("/{projectId}/name")
    public ResponseEntity<ProjectResponseDTO> renameProject(@AuthenticationPrincipal Long userId,
                                                            @PathVariable Long projectId,
                                                            @Valid @RequestBody ProjectRenameRequestDTO request){
        ProjectSummary project = projectService.renameProject(userId, projectId, request.name());

        return ResponseEntity.ok(ProjectResponseDTO.from(project));
    }

    @PatchMapping("/{projectId}/game-system")
    public ResponseEntity<ProjectResponseDTO> updateGameSystem(@AuthenticationPrincipal Long userId,
                                                               @PathVariable Long projectId,
                                                               @Valid @RequestBody ProjectGameSystemUpdateRequestDTO request){
        ProjectSummary project = projectService.updateGameSystem(userId, projectId, request.gameSystem());

        return ResponseEntity.ok(ProjectResponseDTO.from(project));
    }

    @PatchMapping("/{projectId}/description")
    public ResponseEntity<ProjectResponseDTO> updateDescription(@AuthenticationPrincipal Long userId,
                                                               @PathVariable Long projectId,
                                                               @Valid @RequestBody ProjectDescriptionUpdateRequestDTO request){
        ProjectSummary project = projectService.updateDescription(userId, projectId, request.description());

        return ResponseEntity.ok(ProjectResponseDTO.from(project));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@AuthenticationPrincipal Long userId,
                                              @PathVariable Long projectId){
        projectService.deleteProject(userId, projectId);

        return ResponseEntity.noContent().build();
    }
}
