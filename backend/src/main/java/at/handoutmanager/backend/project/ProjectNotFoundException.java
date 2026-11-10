package at.handoutmanager.backend.project;

public class ProjectNotFoundException extends RuntimeException {
  public ProjectNotFoundException(String message) {
    super(message);
  }
}
