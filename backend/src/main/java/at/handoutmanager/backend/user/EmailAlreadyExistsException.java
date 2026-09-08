package at.handoutmanager.backend.user;

// NOTE: Do not expose this message directly in an HTTP response.
// Confirming that a given address is registered enables user enumeration.
// The exception handler must map this to a generic message.
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String value) {
        super("User with email '" + value + "' already exists");
    }
}
