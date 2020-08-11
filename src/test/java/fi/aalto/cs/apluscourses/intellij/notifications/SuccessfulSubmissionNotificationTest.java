package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.Test;

public class SuccessfulSubmissionNotificationTest {

  @Test
  public void testSuccessfulSubmissionNotification() {
    Notification notification = new SuccessfulSubmissionNotification();
    assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    assertEquals("The notification is an information notification",
        NotificationType.INFORMATION, notification.getType());
    assertEquals("The title is correct",
        "Assignment successfully sent for assessment",
        notification.getTitle());
    assertEquals("The content is correct",
        "You will be notified here when feedback is available. "
            + "(You may also always check any of your submissions on the course web site in A+.)",
        notification.getContent()
    );
  }

}
