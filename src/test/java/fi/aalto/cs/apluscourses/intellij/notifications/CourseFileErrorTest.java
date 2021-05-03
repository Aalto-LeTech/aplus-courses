package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.junit.Test;

public class CourseFileErrorTest {

  @Test
  public void testCourseFileError() {
    String errorMessage = "This is just a test! :D";
    Exception e = new Exception(errorMessage);
    Notification notification = new CourseFileError(e);
    assertEquals(PluginSettings.A_PLUS, notification.getGroupId());
    assertEquals(NotificationType.ERROR, notification.getType());
    assertThat(notification.getContent(), containsString(errorMessage));
  }
}
