package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import org.junit.Test;

public class CoursePluginVersionErrorTest {

  @Test
  public void testExerciseNotSelectedNotification() {
    Notification notificationError = new CoursePluginVersionError(true);
    Notification notificationWarning = new CoursePluginVersionError(false);

    assertEquals("Group ID should be A+", "A+", notificationError.getGroupId());
    assertEquals("Notification type should be 'error'",
        NotificationType.ERROR, notificationError.getType());
    assertEquals("Notification type should be 'warning'",
        NotificationType.WARNING, notificationWarning.getType());
  }
}
