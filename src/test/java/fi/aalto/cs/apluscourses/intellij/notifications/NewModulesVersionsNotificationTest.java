package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;

import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.model.ModelExtensions.TestModule;
import fi.aalto.cs.apluscourses.model.Module;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class NewModulesVersionsNotificationTest {

  @Test
  public void testNewModulesVersionsNotificationCreation() {
    //  given
    List<Module> modules = getTwoModulesAsList();

    //  when
    NewModulesVersionsNotification notification =
        new NewModulesVersionsNotification(modules);

    // then
    assertEquals("Group ID should be 'A+'.", "A+", notification.getGroupId());
    assertEquals("Title should be 'Updates available for course modules'.",
        "Updates available for course modules", notification.getTitle());
    assertEquals("Content should contain the names of the Modules to update.",
        "There are newer versions of the following course modules available: "
            + "firstModule, secondModule.",
        notification.getContent());
    assertEquals("The type of the notification should be 'INFORMATION'.",
        NotificationType.INFORMATION, notification.getType());

    List<Module> module = List.of(new TestModule("module"));
    NewModulesVersionsNotification otherNotification =
            new NewModulesVersionsNotification(module);
    assertEquals("Title should be 'Updates available for A+ Course Modules'.",
            "Update available for course module", otherNotification.getTitle());
    assertEquals("Content should contain the names of the Modules to update.",
            "There is a newer version of the following course module available: "
                    + "module.",
            otherNotification.getContent());
  }

  @Test
  public void testGetModuleNameStrings() {
    //  given
    List<Module> modules = getTwoModulesAsList();

    //  when
    String moduleNameStrings = NewModulesVersionsNotification.getModuleNameStrings(modules);

    //  then
    assertEquals("Module names are listed correctly.", "firstModule, secondModule",
        moduleNameStrings);
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
