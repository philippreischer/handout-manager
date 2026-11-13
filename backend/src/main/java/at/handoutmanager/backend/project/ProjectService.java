package at.handoutmanager.backend.project;

import at.handoutmanager.backend.membership.Membership;
import at.handoutmanager.backend.membership.MembershipRole;
import at.handoutmanager.backend.membership.MembershipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public ProjectSummary createProject(Long userId, String name, String nickName){

        Project savedProject = projectRepository.save(new Project(name));

        membershipService.createMembership(userId, savedProject.getId(), nickName, MembershipRole.GAME_MASTER);

        return new ProjectSummary(savedProject.getId(), savedProject.getName(), MembershipRole.GAME_MASTER);
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
                    return new ProjectSummary(project.getId(), project.getName(), membership.getMembershipRole());
                })
                .toList();
    }

    public ProjectSummary getProject(Long userId, Long projectId){

        Membership membership = findMembershipOrThrow(userId, projectId);

        Project project = projectRepository.findById(membership.getProjectId())
                .orElseThrow(()-> new ProjectNotFoundException(
                        "Project does not exist: " + membership.getProjectId()));

        return new ProjectSummary(project.getId(), project.getName(), membership.getMembershipRole());
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
}
