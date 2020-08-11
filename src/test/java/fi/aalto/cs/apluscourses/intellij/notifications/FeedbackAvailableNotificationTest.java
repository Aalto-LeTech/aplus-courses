package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.intellij.notification.Notification;
import fi.aalto.cs.apluscourses.intellij.actions.OpenSubmissionNotificationAction;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import org.junit.Test;

public class FeedbackAvailableNotificationTest {

  @Test
  public void testFeedbackAvailableNotificationTest() {
    SubmissionResult result
        = new SubmissionResult(0, SubmissionResult.Status.GRADED, "https://example.com");
    Notification notification = new FeedbackAvailableNotification(result, "Test Exercise");

    assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    assertThat("The content contains the exercise name", notification.getContent(),
        containsString("Test Exercise"));
    assertTrue("The notification has the correct action",
        notification.getActions().get(0) instanceof OpenSubmissionNotificationAction);
  }

}
