package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NotSubmittableNotificationTest {

  @Test
  void testNotSubmittableNotification() {
    NotSubmittableNotification notification = new NotSubmittableNotification();
    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    MatcherAssert.assertThat("The content mentions the A+ web interface", notification.getContent(),
        containsString("This assignment can only be submitted via a web browser on the "
            + "course website in A+."));
  }

}
