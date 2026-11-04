package at.handoutmanager.backend.project;

import at.handoutmanager.backend.membership.MembershipRole;
import at.handoutmanager.backend.membership.MembershipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
