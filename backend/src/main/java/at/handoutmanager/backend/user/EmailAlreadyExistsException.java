package at.handoutmanager.backend.user;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String value) {
        super("User with email '" + value + "' already exists");
    }
}
