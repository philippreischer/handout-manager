package at.handoutmanager.backend.permission;

import jakarta.persistence.*;

@Entity
@Table(
        name = "permission",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_permission_user_id_resource_id",
                columnNames = {"user_id", "resource_id"}
        )
)

public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    protected Permission() {}

    public Permission(Long userId, Long resourceId) {
        this.userId = userId;
        this.resourceId = resourceId;
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

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
