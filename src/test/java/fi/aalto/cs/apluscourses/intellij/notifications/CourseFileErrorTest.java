package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseFileErrorTest {

  @Test
  void testCourseFileError() {
    String errorMessage = "This is just a test! :D";
    Exception e = new Exception(errorMessage);
    Notification notification = new CourseFileError(e);
    Assertions.assertEquals(PluginSettings.A_PLUS, notification.getGroupId());
    Assertions.assertEquals(NotificationType.ERROR, notification.getType());
    MatcherAssert.assertThat(notification.getContent(), containsString(errorMessage));
  }
}
