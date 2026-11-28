package at.handoutmanager.backend.project;

import at.handoutmanager.backend.common.StringUtil;
import at.handoutmanager.backend.membership.Membership;
import at.handoutmanager.backend.membership.MembershipRole;
import at.handoutmanager.backend.membership.MembershipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MembershipService membershipService;

    public ProjectService(ProjectRepository projectRepository, MembershipService membershipService) {
        this.projectRepository = projectRepository;
        this.membershipService = membershipService;
    }

    @Transactional
    public ProjectSummary createProject(Long userId, String name, String gameSystem, String description, String nickName){
        String normalizedGameSystem = StringUtil.blankToNull(gameSystem);
        String normalizedDescription = StringUtil.blankToNull(description);
        Project savedProject = projectRepository.save(new Project(name, normalizedGameSystem, normalizedDescription));

        membershipService.createMembership(userId, savedProject.getId(), nickName, MembershipRole.GAME_MASTER);

        return ProjectSummary.of(savedProject, MembershipRole.GAME_MASTER);
    }


    public List<ProjectSummary> getProjectsForUser(Long userId){
        List<Membership> memberships = membershipService.getMembershipsByUserId(userId);

        List<Long> projectIds = memberships.stream()
                .map(Membership::getProjectId)
                .toList();

        Map<Long, Project> projectsById = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));

        return memberships.stream()
                .map(membership -> {
                    Project project = projectsById.get(membership.getProjectId());
                    return ProjectSummary.of(project, membership.getMembershipRole());
                })
                .toList();
    }

    public ProjectSummary getProject(Long userId, Long projectId){
        Membership membership = findMembershipOrThrow(userId, projectId);

        Project project = findProjectOrThrow(membership.getProjectId());

        return ProjectSummary.of(project, membership.getMembershipRole());
    }

    @Transactional
    public ProjectSummary renameProject(Long userId, Long projectId, String name){
        Membership membership = findMembershipOrThrow(userId, projectId);
        requireGameMaster(membership);

        Project project = findProjectOrThrow(membership.getProjectId());

        project.setName(name);
        Project savedProject = projectRepository.save(project);

        return ProjectSummary.of(savedProject, membership.getMembershipRole());
    }

    @Transactional
    public ProjectSummary updateGameSystem(Long userId, Long projectId, String gameSystem){
        Membership membership = findMembershipOrThrow(userId, projectId);
        requireGameMaster(membership);

        Project project = findProjectOrThrow(membership.getProjectId());

        String normalizedGameSystem = StringUtil.blankToNull(gameSystem);
        project.setGameSystem(normalizedGameSystem);
        Project savedProject = projectRepository.save(project);

        return ProjectSummary.of(savedProject, membership.getMembershipRole());
    }

    @Transactional
    public ProjectSummary updateDescription(Long userId, Long projectId, String description){
        Membership membership = findMembershipOrThrow(userId, projectId);
        requireGameMaster(membership);

        Project project = findProjectOrThrow(membership.getProjectId());

        String normalizedDescription = StringUtil.blankToNull(description);
        project.setDescription(normalizedDescription);
        Project savedProject = projectRepository.save(project);

        return ProjectSummary.of(savedProject, membership.getMembershipRole());
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId){
        Membership membership = findMembershipOrThrow(userId, projectId);
        requireGameMaster(membership);

        Project project = findProjectOrThrow(membership.getProjectId());

        projectRepository.delete(project);
    }

    @Transactional
    public void deleteProjectsOwnedBy(Long userId){
        List<Membership> memberships = membershipService.getMembershipsByUserId(userId);

        List<Long> projectIds = memberships.stream()
                .filter(membership -> membership.getMembershipRole() == MembershipRole.GAME_MASTER)
                .map(Membership::getProjectId)
                .toList();

        projectRepository.deleteAllByIdInBatch(projectIds);
    }

    private Membership findMembershipOrThrow(Long userId, Long projectId){
        return membershipService.findMembership(userId, projectId)
                .orElseThrow(()-> new ProjectNotFoundException(
                        "No membership found, userId: " + userId + ", projectId: " + projectId));
    }

    private void requireGameMaster(Membership membership){
        if (membership.getMembershipRole() != MembershipRole.GAME_MASTER){
                throw new ProjectAccessDeniedException(
                        "userId: " + membership.getUserId() + ", projectId: " + membership.getProjectId());
        }
    }

    private Project findProjectOrThrow(Long projectId){
        return projectRepository.findById(projectId)
                    .orElseThrow(()-> new ProjectNotFoundException(
                            "Project does not exist: " + projectId));
    }
}
