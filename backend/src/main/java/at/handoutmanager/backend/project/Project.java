package at.handoutmanager.backend.project;

import at.handoutmanager.backend.common.Auditable;
import jakarta.persistence.*;

@Entity
@Table(name = "project")

public class Project extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "game_system", length = 255)
    private String gameSystem;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    protected Project(){}

    public Project(String name, String gameSystem, String description) {
        this.name = name;
        this.gameSystem = gameSystem;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getGameSystem() {
        return gameSystem;
    }

    public void setGameSystem(String gameSystem) {
        this.gameSystem = gameSystem;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
