package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.jupiter.api.Test;

public class SubmissionSentNotificationTest {

  @Test
  public void testSuccessfulSubmissionNotification() {
    Notification notification = new SubmissionSentNotification();
    assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    assertEquals("The notification is an information notification",
        NotificationType.INFORMATION, notification.getType());
    assertEquals("The title is correct",
        "Assignment sent for assessment",
        notification.getTitle());
    assertEquals("The content is correct",
        "You will be notified here when feedback is available. "
            + "(You may also always check any of your submissions on the course website in A+.)",
        notification.getContent()
    );
  }

}
