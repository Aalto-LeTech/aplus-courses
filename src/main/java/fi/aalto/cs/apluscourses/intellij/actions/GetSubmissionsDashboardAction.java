package fi.aalto.cs.apluscourses.intellij.actions;

import static java.util.Objects.requireNonNull;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.SubmissionResultsList;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    Project project = e.getProject();
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);

    APlusAuthenticationViewModel authenticationViewModel =
        mainViewModel.getAuthenticationViewModel().get();
    if (authenticationViewModel == null) {
      return;
    }

    APlusAuthentication authentication =
        requireNonNull(mainViewModel.getAuthenticationViewModel().get()).getAuthentication();
    Course course = requireNonNull(mainViewModel.getCourseViewModel().get()).getModel();

    if (authentication != null) {
      SubmissionResultsList submissionResultsList = tryGetSubmissionsDashboard(
          Long.parseLong(course.getId()), authentication, requireNonNull(project));
      course.setSubmissionsDashboard(requireNonNull(submissionResultsList));
    }
  }

  /**
   * A wrapper method for a method that pulls the A+ API for course exercises' results.
   *
   * @param courseId       an id of the course results data to pull.
   * @param authentication an {@link APlusAuthentication} object to use with API.
   * @param project        a current {@link Project} to notify to.
   * @return a fully-inflated {@link SubmissionResultsList} or null if the API call was
   *        unsuccessful.
   */
  @Nullable
  public SubmissionResultsList tryGetSubmissionsDashboard(long courseId,
                                                          @NotNull
                                                              APlusAuthentication authentication,
                                                          @NotNull Project project) {
    try {
      return SubmissionResultsList.getSubmissionResultsList(courseId, authentication);
    } catch (IOException e) {
      notifyNetworkError(e, project);
      return null;
    }
  }

  private void notifyNetworkError(@NotNull IOException exception, @Nullable Project project) {
    notifier.notify(new NetworkErrorNotification(exception), project);
  }
}
