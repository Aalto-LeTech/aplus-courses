package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MissingModuleNotificationTest {

  @Test
  void testMissingModuleNotification() {
    String name = "module name";
    MissingModuleNotification notification = new MissingModuleNotification(name);
    Assertions.assertEquals("Could not find module", notification.getTitle(), "The title is correct");
    Assertions.assertEquals(name, notification.getModuleName(), "Module name should be correct");
    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    MatcherAssert.assertThat("The content should contain the module name", notification.getContent(),
        containsString(name));
  }

}
