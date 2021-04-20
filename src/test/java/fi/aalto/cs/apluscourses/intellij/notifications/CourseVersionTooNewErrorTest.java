package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.Test;

public class CourseVersionTooNewErrorTest {

  @Test
  public void testCourseVersionTooNewNotification() {
    Notification notification = new CourseVersionTooNewError();

    assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    assertEquals("Notification type should be 'error'",
        NotificationType.ERROR, notification.getType());
  }
}
