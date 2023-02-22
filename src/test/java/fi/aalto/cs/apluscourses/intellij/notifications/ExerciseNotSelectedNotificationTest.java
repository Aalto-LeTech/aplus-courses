package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import com.intellij.notification.Notification;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExerciseNotSelectedNotificationTest {

  @Test
  void testExerciseNotSelectedNotification() {
    Notification notification = new ExerciseNotSelectedNotification();
    Assertions.assertEquals("No assignment is selected", notification.getTitle(), "The title is correct");
    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    MatcherAssert.assertThat(notification.getContent(), containsString("To submit an assignment, please "
        + "select the assignment in the list before clicking the submit button."));
  }

}
