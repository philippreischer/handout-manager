package at.handoutmanager.backend.project;

import at.handoutmanager.backend.membership.MembershipRole;

public record ProjectSummary(Long id,
                             String name,
                             MembershipRole membershipRole) {
}
