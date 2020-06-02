package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.when;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.HeavyPlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.util.Set;
import org.junit.Test;
import org.mockito.Mockito;

public class MainViewModelUpdaterTest extends HeavyPlatformTestCase implements TestHelper {

  @Test
  public void testGetProjectModuleNamesWithCorrectModulesReturnsValid() {
    //  given
    Project project = getProject();
    createAndAddModule(project, "module1", "UID#1");
    //  modules are identified by name, so only one is actually created
    createAndAddModule(project, "module1", "UID#1");
    createAndAddModule(project, "module2", "UID#1");
    Module[] modules = ModuleManager.getInstance(project).getModules();
    assertEquals("Only one module named 'module1' is created.", 3, modules.length);

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    MainViewModelUpdater mainViewModelUpdater = new MainViewModelUpdater(mainViewModel,
        project, 1000L);

    //  when
    Set<String> projectModules = mainViewModelUpdater.getProjectModuleNames();

    //  then
    assertEquals("There are 3 (three) module names listed.", 3, projectModules.size());
  }

  @Test
  public void testGetProjectModuleNamesWithClosedProjectReturnsEmpty() {
    //  given
    Project project = getProject();
    Project spyProject = Mockito.spy(project);
    when(spyProject.isOpen()).thenReturn(false);

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(spyProject);
    MainViewModelUpdater mainViewModelUpdater = new MainViewModelUpdater(mainViewModel,
        spyProject, 1000L);

    //  when
    Set<String> projectModules = mainViewModelUpdater.getProjectModuleNames();

    //  then
    assertEmpty("There are 0 (zero) module names listed for a closed project.", projectModules);
  }
}