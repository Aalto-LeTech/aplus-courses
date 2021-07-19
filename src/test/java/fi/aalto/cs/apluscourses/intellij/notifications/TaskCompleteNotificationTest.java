package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.junit.Assert;
import org.junit.Test;

public class TaskCompleteNotificationTest {

  @Test
  public void testTaskAlreadyCompleteNotification() {
    TaskCompleteNotification notificationAlready =
        TaskCompleteNotification.createTaskAlreadyCompleteNotification(0, "instructions");
    Assert.assertEquals("Group ID should be A+", "A+", notificationAlready.getGroupId());
    Assert.assertThat("The content mentions the already completed Task", notificationAlready.getContent(),
        containsString("Task 1 is already complete"));
  }

  @Test
  public void testTaskCompleteNotification() {
    TaskCompleteNotification notification = TaskCompleteNotification.createTaskCompleteNotification(1);
    Assert.assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    Assert.assertThat("The content mentions the Task that was just completed by the user", notification.getContent(),
        containsString("Task 2 is now complete"));
  }

}
