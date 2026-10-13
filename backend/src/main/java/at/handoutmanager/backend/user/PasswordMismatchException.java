package at.handoutmanager.backend.user;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String operation) {
        super(operation + " failed: wrong password");
    }
}
