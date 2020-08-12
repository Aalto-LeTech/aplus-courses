package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.intellij.notification.Notification;
import org.junit.Test;

public class ExerciseNotSelectedNotificationTest {

  @Test
  public void testExerciseNotSelectedNotification() {
    Notification notification = new ExerciseNotSelectedNotification();
    assertEquals("The title is correct", "No assignment is selected", notification.getTitle());
    assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    assertThat(notification.getContent(), containsString("To submit an assignment, please "
        + "select the assignment in the list before clicking the submit button."));
  }

}
