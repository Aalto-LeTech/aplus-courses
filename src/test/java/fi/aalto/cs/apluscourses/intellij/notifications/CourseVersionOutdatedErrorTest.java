package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseVersionOutdatedErrorTest {

  @Test
  void testCourseVersionOutdatedNotification() {
    Notification notification = new CourseVersionOutdatedError();

    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    Assertions.assertEquals(NotificationType.ERROR, notification.getType(), "Notification type should be 'error'");
  }
}
