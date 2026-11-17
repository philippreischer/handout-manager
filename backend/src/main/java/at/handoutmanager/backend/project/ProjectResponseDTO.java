package at.handoutmanager.backend.project;

import at.handoutmanager.backend.membership.MembershipRole;

public record ProjectResponseDTO(Long id,
                                 String name,
                                 MembershipRole membershipRole) {
}
