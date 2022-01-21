package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseVersionOutdatedWarningTest {

  @Test
  void testCourseVersionOutdatedNotification() {
    Notification notification = new CourseVersionOutdatedWarning();

    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    Assertions.assertEquals(NotificationType.WARNING, notification.getType(), "Notification type should be 'warning'");
  }
}
