package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.intellij.notification.Notification;
import fi.aalto.cs.apluscourses.intellij.actions.OpenSubmissionNotificationAction;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import org.junit.Test;

public class FeedbackAvailableNotificationTest {

  @Test
  public void testFeedbackAvailableNotificationTest() {
    Exercise exercise = new Exercise(123, "Test Exercise", "https://example.com", 3, 5, 10, true);
    SubmissionResult result
        = new SubmissionResult(0, 0, SubmissionResult.Status.GRADED, exercise);
    Notification notification = new FeedbackAvailableNotification(result, exercise);

    assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    assertThat("The content contains the exercise name", notification.getContent(),
        containsString("Test Exercise"));
    assertTrue("The notification has the correct action",
        notification.getActions().get(0) instanceof OpenSubmissionNotificationAction);
  }

}
