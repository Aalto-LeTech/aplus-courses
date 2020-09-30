package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.FeedbackAvailableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

public class SubmissionStatusUpdaterTest {

  static class TestDataSource extends ModelExtensions.TestExerciseDataSource {

    private int limit = 3;
    private final AtomicInteger submissionResultFetchCount = new AtomicInteger(0);

    public int getSubmissionResultFetchCount() {
      return submissionResultFetchCount.get();
    }

    @NotNull
    @Override
    public SubmissionResult getSubmissionResult(@NotNull String submissionUrl,
                                                @NotNull Exercise exercise,
                                                @NotNull Authentication authentication) {

      return new SubmissionResult(
          123L,
          0,
          submissionResultFetchCount.incrementAndGet() >= limit
              ? SubmissionResult.Status.GRADED : SubmissionResult.Status.UNKNOWN,
          exercise
      );
    }
  }

  private TestDataSource dataSource;
  private Notifier notifier;

  @Before
  public void setUp() {
    dataSource = new TestDataSource();
    notifier = mock(Notifier.class);
  }

  @Test
  public void testSubmissionStatusUpdater() throws InterruptedException {
    new SubmissionStatusUpdater(
        mock(Project.class),
        dataSource,
        mock(Authentication.class),
        notifier,
        "http://localhost:1000",
        new Exercise(789, "Cool Exercise Name", "http://example.com", 0, 5, 10),
        25L, // 0.025 second interval
        0L, // don't increment the interval at all
        10000L // 10 second time limit, which shouldn't be reached
    ).start();
    Thread.sleep(800L);

    assertEquals("The submission results are not fetched anymore after feedback is available",
        3, dataSource.getSubmissionResultFetchCount());
    verify(notifier).notify(any(FeedbackAvailableNotification.class), any(Project.class));
  }

  @Test
  public void testSubmissionStatusUpdaterTimeLimit() throws InterruptedException {
    dataSource.limit = 9999;
    new SubmissionStatusUpdater(
        mock(Project.class),
        dataSource,
        mock(Authentication.class),
        notifier,
        "http://localhost:1000",
        new Exercise(789, "Cool Exercise Name", "http://example.com", 0, 5, 10),
        25L, // 0.025 second interval
        0L, // don't increment the interval at all
        200L // 0.2 second time limit, should update at most 8 times
    ).start();
    Thread.sleep(800L);
    assertTrue(dataSource.getSubmissionResultFetchCount() <= 8);
    verifyNoInteractions(notifier);
  }

}
