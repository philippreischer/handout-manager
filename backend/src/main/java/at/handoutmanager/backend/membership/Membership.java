package at.handoutmanager.backend.membership;

import jakarta.persistence.*;

@Entity
@Table(
        name = "membership",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_membership_user_id_project_id",
                columnNames = {"user_id", "project_id"}
        )
)

public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "nick_name", length = 255)
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_role", nullable = false, length = 30)
    private MembershipRole membershipRole;

    protected Membership() {}

    public Membership(Long userId, Long projectId, String nickName, MembershipRole membershipRole) {
        this.userId = userId;
        this.projectId = projectId;
        this.nickName = nickName;
        this.membershipRole = membershipRole;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public MembershipRole getMembershipRole() {
        return membershipRole;
    }

    public void setMembershipRole(MembershipRole membershipRole) {
        this.membershipRole = membershipRole;
    }
}
