package at.handoutmanager.backend.user;

import at.handoutmanager.backend.common.Auditable;
import jakarta.persistence.*;

@Entity
@Table(
        name = "user_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_account_email", columnNames = "email")
        }
)

public class User extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;


    protected User(){}

    public User(String email, String passwordHash, String userName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.userName = userName;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
