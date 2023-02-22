package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import java.nio.file.Paths;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MissingFileNotificationTest {

  @Test
  void testMissingFileNotification() {
    MissingFileNotification notification = new MissingFileNotification(
        Paths.get("awesome_module"), "awesome file");
    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    MatcherAssert.assertThat("The content should contain the path", notification.getContent(),
        containsString("awesome_module"));
    MatcherAssert.assertThat("The content should contain the filename", notification.getContent(),
        containsString("awesome file"));
  }

}
