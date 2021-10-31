package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.utils.Event;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class ToolWindowCardViewModel {
  public final Event updated = new Event();

  private boolean isAuthenticated;

  private boolean isNetworkError;

  private boolean isAPlusProject;

  @NotNull
  private final AtomicBoolean isProjectReady = new AtomicBoolean(false);

  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  public void setAuthenticated(boolean authenticated) {
    isAuthenticated = authenticated;
  }

  public boolean isProjectReady() {
    return isProjectReady.get();
  }

  /**
   * Returns true if the value changed, false if the value was already equal to the given value.
   */
  public boolean setProjectReady(boolean projectReady) {
    return isProjectReady.getAndSet(projectReady) != projectReady;
  }

  public boolean isNetworkError() {
    return isNetworkError;
  }

  public void setNetworkError(boolean networkError) {
    isNetworkError = networkError;
  }

  public boolean isAPlusProject() {
    return isAPlusProject;
  }

  public void setAPlusProject(boolean aplusProject) {
    isAPlusProject = aplusProject;
  }
}
