package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class GetSubmissionsDashboardAction extends DumbAwareAction {

  public static final String ACTION_ID = GetSubmissionsDashboardAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Notifier notifier;

  /**
   * Constructs the action with meaningful defaults.
   */
  public GetSubmissionsDashboardAction() {
    this(PluginSettings.getInstance(), Notifications.Bus::notify);
  }

  public GetSubmissionsDashboardAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                       @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.notifier = notifier;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    /*
    This action will eventually be a generic update action, which updates the main view model. For
    now it is disabled, since the points list is fetched as a "by-product" of the main view model
    update that occurs every 15 minutes, and this potentially causes a NPE.
     */
  }

}
