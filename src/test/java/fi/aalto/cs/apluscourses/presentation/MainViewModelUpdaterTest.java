package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class MainViewModelUpdaterTest {

  @Test
  public void testOnlyShowUpdateNotificationOnce() {
    Module module1 = new ModelExtensions.TestModule("Module1");
    Module module2 = new ModelExtensions.TestModule("Module2");
    List<Module> modules = Arrays.asList(module1, module2);

    MainViewModel mainViewModel = new MainViewModel(new Options());
    Project project = mock(Project.class);
    Notifier notifier = mock(Notifier.class);
    PasswordStorage.Factory passwordStorageFactory = mock(PasswordStorage.Factory.class);

    MainViewModelUpdater updater = new MainViewModelUpdater(
            mainViewModel, project, 10000, notifier, passwordStorageFactory
    );

    assertEquals("All modules returned",
            updater.filterOldUpdatableModules(modules), modules);
    assertEquals("No modules returned",
            updater.filterOldUpdatableModules(modules), new ArrayList<Module>());
  }
}