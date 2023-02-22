package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TaskCompleteNotificationTest {

  @Test
  void testTaskAlreadyCompleteNotification() {
    TaskCompleteNotification notificationAlready =
        TaskCompleteNotification.createTaskAlreadyCompleteNotification(0, "instructions");
    Assertions.assertEquals("A+", notificationAlready.getGroupId(), "Group ID should be A+");
    MatcherAssert.assertThat("The content mentions the already completed Task", notificationAlready.getContent(),
        containsString("Task 1 is already complete"));
  }

  @Test
  void testTaskCompleteNotification() {
    TaskCompleteNotification notification = TaskCompleteNotification.createTaskCompleteNotification(1);
    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    MatcherAssert.assertThat("The content mentions the Task that was just completed by the user",
        notification.getContent(), containsString("Task 2 is now complete"));
  }

}
