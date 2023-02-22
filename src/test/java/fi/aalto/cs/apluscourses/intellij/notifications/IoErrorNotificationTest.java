package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IoErrorNotificationTest {

  @Test
  void testIoErrorNotification() {
    IOException exception = new IOException("hello there");
    IoErrorNotification notification = new IoErrorNotification(exception);
    Assertions.assertEquals("A+", notification.getGroupId(), "The notification has the correct group ID");
    MatcherAssert.assertThat("The exception message is in the notification content", notification.getContent(),
        containsString("hello there"));
  }

}
