package at.handoutmanager.backend.membership;

import at.handoutmanager.backend.common.StringUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;

    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public Membership createMembership(Long userId, Long projectId,
                                       String nickName, MembershipRole membershipRole){
        String normalizedNickName = StringUtil.blankToNull(nickName);
        Membership membership = new Membership(userId, projectId, normalizedNickName, membershipRole);

        return membershipRepository.save(membership);
    }

    public List<Membership> getMembershipsByUserId(Long userId){
        return membershipRepository.findByUserId(userId);
    }

    public Optional<Membership> findMembership(Long userId, Long projectId){
        return membershipRepository.findByUserIdAndProjectId(userId, projectId);
    }
}
