package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.ui.toolwindowcards.ToolWindowCardView.ERROR_CARD;
import static fi.aalto.cs.apluscourses.ui.toolwindowcards.ToolWindowCardView.LOADING_CARD;
import static fi.aalto.cs.apluscourses.ui.toolwindowcards.ToolWindowCardView.MAIN_CARD;
import static fi.aalto.cs.apluscourses.ui.toolwindowcards.ToolWindowCardView.NO_TOKEN_CARD;
import static fi.aalto.cs.apluscourses.ui.toolwindowcards.ToolWindowCardView.PROJECT_CARD;

import fi.aalto.cs.apluscourses.utils.Event;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class ToolWindowCardViewModel {
  public final Event updated = new Event();

  private boolean isAuthenticated;

  private boolean isNetworkError;

  private boolean isAPlusProject;

  private boolean moduleButtonRequiresLogin;

  @NotNull
  private final AtomicBoolean isProjectReady = new AtomicBoolean(false);

  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  public void setAuthenticated(boolean authenticated) {
    isAuthenticated = authenticated;
    updated.trigger();
  }

  public boolean isProjectReady() {
    return isProjectReady.get();
  }

  /**
   * Returns true if the value changed, false if the value was already equal to the given value.
   */
  public boolean setProjectReady(boolean projectReady) {
    var wasReady = isProjectReady.getAndSet(projectReady) != projectReady;
    updated.trigger();
    return wasReady;
  }

  public boolean isNetworkError() {
    return isNetworkError;
  }

  public void setNetworkError(boolean networkError) {
    isNetworkError = networkError;
    updated.trigger();
  }

  public boolean isAPlusProject() {
    return isAPlusProject;
  }

  public void setAPlusProject(boolean aplusProject) {
    isAPlusProject = aplusProject;
    updated.trigger();
  }

  public boolean moduleButtonRequiresLogin() {
    return moduleButtonRequiresLogin;
  }

  public void setModuleButtonRequiresLogin(boolean requiresLogin) {
    moduleButtonRequiresLogin = requiresLogin;
    updated.trigger();
  }
}
