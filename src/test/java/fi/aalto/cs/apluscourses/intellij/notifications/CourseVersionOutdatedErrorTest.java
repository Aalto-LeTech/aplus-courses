package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.jupiter.api.Test;

public class CourseVersionOutdatedErrorTest {

  @Test
  public void testCourseVersionOutdatedNotification() {
    Notification notification = new CourseVersionOutdatedError();

    assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    assertEquals("Notification type should be 'error'",
        NotificationType.ERROR, notification.getType());
  }
}
