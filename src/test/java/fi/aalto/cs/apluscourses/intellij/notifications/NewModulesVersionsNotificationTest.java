package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.model.ModelExtensions.TestModule;
import fi.aalto.cs.apluscourses.model.Module;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NewModulesVersionsNotificationTest {

  @Test
  void testNewModulesVersionsNotificationCreation() {
    //  given
    List<Module> modules = getTwoModulesAsList();

    //  when
    NewModulesVersionsNotification notification =
        new NewModulesVersionsNotification(modules);

    // then
    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be 'A+'.");
    Assertions.assertEquals("Updates available for course modules", notification.getTitle(),
        "Title should be 'Updates available for course modules'.");
    Assertions.assertEquals("There are newer versions of the following course modules available: "
            + "firstModule, secondModule.", notification.getContent(),
        "Content should contain the names of the Modules to update.");
    Assertions.assertEquals(NotificationType.INFORMATION, notification.getType(),
        "The type of the notification should be 'INFORMATION'.");

    List<Module> module = List.of(new TestModule("module"));
    NewModulesVersionsNotification otherNotification =
        new NewModulesVersionsNotification(module);
    Assertions.assertEquals("Update available for course module", otherNotification.getTitle(),
        "Title should be 'Updates available for A+ Course Modules'.");
    Assertions.assertEquals("There is a newer version of the following course module available: "
        + "module.", otherNotification.getContent(), "Content should contain the names of the Modules to update.");
  }

  @Test
  void testGetModuleNameStrings() {
    //  given
    List<Module> modules = getTwoModulesAsList();

    //  when
    String moduleNameStrings = NewModulesVersionsNotification.getModuleNameStrings(modules);

    //  then
    Assertions.assertEquals("firstModule, secondModule", moduleNameStrings, "Module names are listed correctly.");
  }

  private List<Module> getTwoModulesAsList() {
    TestModule moduleOne = new TestModule("firstModule");
    TestModule moduleTwo = new TestModule("secondModule");
    List<Module> modules = new ArrayList<>();
    modules.add(moduleOne);
    modules.add(moduleTwo);
    return modules;
  }
}
