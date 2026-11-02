package at.handoutmanager.backend.membership;

import org.springframework.stereotype.Service;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;

    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public Membership createMembership(Long userId, Long projectId,
                                       String nickName, MembershipRole membershipRole){

        String normalizedNickName = normalizeNickName(nickName);

        Membership membership = new Membership(userId, projectId, normalizedNickName, membershipRole);

        return membershipRepository.save(membership);
    }


    private String normalizeNickName(String nickName){

        if (nickName == null || nickName.isBlank()){
            return null;
        }
        return nickName;
    }
}
