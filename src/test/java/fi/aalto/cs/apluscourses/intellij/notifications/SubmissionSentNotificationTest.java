package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubmissionSentNotificationTest {

  @Test
  void testSuccessfulSubmissionNotification() {
    Notification notification = new SubmissionSentNotification();
    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    Assertions.assertEquals(NotificationType.INFORMATION, notification.getType(),
        "The notification is an information notification");
    Assertions.assertEquals("Assignment sent for assessment", notification.getTitle(), "The title is correct");
    Assertions.assertEquals("You will be notified here when feedback is available. "
            + "(You may also always check any of your submissions on the course website in A+.)",
        notification.getContent(), "The content is correct");
  }

}
