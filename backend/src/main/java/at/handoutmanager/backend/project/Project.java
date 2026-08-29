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

        protected Project(){}

        public Project(String name){
                this.name = name;
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
}
