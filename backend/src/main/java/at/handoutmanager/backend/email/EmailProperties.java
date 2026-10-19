package at.handoutmanager.backend.email;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "handout-manager.email")
public class EmailProperties {

    /**
     * Sender address of every outgoing mail. Mailtrap accepts anything here,
     * a real provider only accepts a verified domain - so this is configuration,
     * never a literal in the code.
     */
    @NotBlank
    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
