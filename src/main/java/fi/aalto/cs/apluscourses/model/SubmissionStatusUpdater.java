package fi.aalto.cs.apluscourses.model;

import com.intellij.notification.Notifications;
import fi.aalto.cs.apluscourses.intellij.notifications.FeedbackAvailableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class SubmissionStatusUpdater {

  @NotNull
  private final ExerciseDataSource dataSource;

  @NotNull
  private final Authentication authentication;

  @NotNull
  private final Notifier notifier;

  @NotNull
  private final String submissionUrl;

  @NotNull
  private final String exerciseName;

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
  public SubmissionStatusUpdater(@NotNull ExerciseDataSource dataSource,
                                 @NotNull Authentication authentication,
                                 @NotNull Notifier notifier,
                                 @NotNull String submissionUrl,
                                 @NotNull String exerciseName,
                                 long interval,
                                 long increment,
                                 long timeLimit) {
    this.dataSource = dataSource;
    this.authentication = authentication;
    this.notifier = notifier;
    this.submissionUrl = submissionUrl;
    this.exerciseName = exerciseName;
    this.interval = interval;
    this.increment = increment;
    this.timeLimit = timeLimit;
    thread = new Thread(this::run);
    this.totalTime = 0;
  }

  /**
   * Construct a submission status updater with reasonable defaults for the time values.
   */
  public SubmissionStatusUpdater(@NotNull ExerciseDataSource dataSource,
                                 @NotNull Authentication authentication,
                                 @NotNull String submissionUrl,
                                 @NotNull String exerciseName) {
    this(
        dataSource,
        authentication,
        Notifications.Bus::notify,
        submissionUrl,
        exerciseName,
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
      while (true) { //NOSONAR
        if (totalTime >= timeLimit) {
          return;
        }

        SubmissionResult submissionResult;
        try {
          submissionResult = dataSource.getSubmissionResult(submissionUrl, authentication);
          if (submissionResult.getStatus() != SubmissionResult.Status.UNKNOWN) {
            notifier.notify(new FeedbackAvailableNotification(submissionResult, exerciseName),null);
            return;
          }
        } catch (IOException e) {
          // Fail silently
        }

        Thread.sleep(interval);
        totalTime += interval;
        interval += increment;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
