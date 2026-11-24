package at.handoutmanager.backend.project;

import at.handoutmanager.backend.membership.MembershipRole;

public record ProjectResponseDTO(Long id,
                                 String name,
                                 String gameSystem,
                                 String description,
                                 MembershipRole membershipRole) {

    public static ProjectResponseDTO from(ProjectSummary summary) {
        return new ProjectResponseDTO(
                summary.id(),
                summary.name(),
                summary.gameSystem(),
                summary.description(),
                summary.membershipRole());
    }
}
