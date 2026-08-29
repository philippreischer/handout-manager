package at.handoutmanager.backend.resource;

import at.handoutmanager.backend.common.Auditable;
import jakarta.persistence.*;

@Entity
@Table(name = "resource")
public class Resource extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_path", length = 255)
    private String filePath;

    protected Resource() {}

    public Resource(Long projectId,
                    Long createdBy,
                    String name,
                    ResourceType resourceType,
                    String content,
                    String filePath) {
        this.projectId = projectId;
        this.createdBy = createdBy;
        this.name = name;
        this.resourceType = resourceType;
        this.content = content;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
