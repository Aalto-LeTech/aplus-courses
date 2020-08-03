package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.Assert;
import org.junit.Test;

public class SuccessfulSubmissionNotificationTest {

  @Test
  public void testSuccessfulSubmissionNotification() {
    Notification notification = new SuccessfulSubmissionNotification();
    Assert.assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    Assert.assertEquals("The notification is an information notification",
        NotificationType.INFORMATION, notification.getType());
    Assert.assertEquals("The title is correct", "Exercise submitted successfully",
        notification.getTitle());
    Assert.assertEquals(
        "The content is correct",
        "You will be notified when feedback for the exercise is available.",
        notification.getContent()
    );
  }

}
