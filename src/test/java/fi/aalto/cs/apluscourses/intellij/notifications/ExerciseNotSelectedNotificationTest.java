package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import com.intellij.notification.Notification;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseNotSelectedNotificationTest {

  @Test
  public void testExerciseNotSelectedNotification() {
    Notification notification = new ExerciseNotSelectedNotification();
    Assert.assertEquals("The title is correct", "No exercise is selected", notification.getTitle());
    Assert.assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    Assert.assertThat(notification.getContent(), containsString("Select an exercise"));
  }

}
