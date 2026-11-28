package at.handoutmanager.backend.project;

import at.handoutmanager.backend.membership.MembershipRole;

public record ProjectSummary(Long id,
                             String name,
                             String gameSystem,
                             String description,
                             MembershipRole membershipRole) {

    public static ProjectSummary of(Project project, MembershipRole membershipRole) {
        return new ProjectSummary(
                project.getId(),
                project.getName(),
                project.getGameSystem(),
                project.getDescription(),
                membershipRole);
    }
}
