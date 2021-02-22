package fi.aalto.cs.apluscourses.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.FeedbackAvailableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionStatusUpdater {

  @Nullable
  private final Project project;

  @NotNull
  private final ExerciseDataSource dataSource;

  @NotNull
  private final Authentication authentication;

  @NotNull
  private final Notifier notifier;

  @NotNull
  private final String submissionUrl;

  @NotNull
  private final Exercise exercise;

  private long interval;

  private final long increment;

  private final long timeLimit;

  private final Thread thread;

  private long totalTime;

  // 10 seconds in milliseconds
  private static final long DEFAULT_INTERVAL = 10L * 1000;

  // 5 seconds in milliseconds
  private static final long DEFAULT_INCREMENT = 5L * 1000;

  // 3 minutes in milliseconds
  private static final long DEFAULT_TIME_LIMIT = 3L * 60 * 1000;

  /**
   * Construct a submission status updater with the given parameters.
   */
  public SubmissionStatusUpdater(@Nullable Project project,
                                 @NotNull ExerciseDataSource dataSource,
                                 @NotNull Authentication authentication,
                                 @NotNull Notifier notifier,
                                 @NotNull String submissionUrl,
                                 @NotNull Exercise exercise,
                                 long interval,
                                 long increment,
                                 long timeLimit) {
    this.project = project;
    this.dataSource = dataSource;
    this.authentication = authentication;
    this.notifier = notifier;
    this.submissionUrl = submissionUrl;
    this.exercise = exercise;
    this.interval = interval;
    this.increment = increment;
    this.timeLimit = timeLimit;
    thread = new Thread(this::run);
    this.totalTime = 0;
  }

  /**
   * Construct a submission status updater with reasonable defaults for the time values.
   */
  public SubmissionStatusUpdater(@Nullable Project project,
                                 @NotNull ExerciseDataSource dataSource,
                                 @NotNull Authentication authentication,
                                 @NotNull String submissionUrl,
                                 @NotNull Exercise exercise) {
    this(
        project,
        dataSource,
        authentication,
        new DefaultNotifier(),
        submissionUrl,
        exercise,
        DEFAULT_INTERVAL,
        DEFAULT_INCREMENT,
        DEFAULT_TIME_LIMIT
    );
  }

  public void start() {
    thread.start();
  }

  private void run() {
    try {
      while (true) { //  NOSONAR
        if (totalTime >= timeLimit) {
          return;
        }

        if (fetchResultsAndNotify()) {
          return;
        }

        Thread.sleep(interval);
        totalTime += interval;
        interval += increment;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private boolean fetchResultsAndNotify() {
    SubmissionResult submissionResult;
    try {
      submissionResult =
          dataSource.getSubmissionResult(submissionUrl, exercise, authentication);
      if (submissionResult.getStatus() != SubmissionResult.Status.UNKNOWN) {
        notifier.notifyAndHide(
            new FeedbackAvailableNotification(submissionResult, exercise), project);
        PluginSettings.getInstance().updateMainViewModel(project);
        return true;
      }
    } catch (IOException e) {
      // Fail silently
    }
    return false;
  }

}
