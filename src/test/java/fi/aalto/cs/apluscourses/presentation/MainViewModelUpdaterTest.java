package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.HeavyPlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MainViewModelUpdaterTest extends HeavyPlatformTestCase implements TestHelper {

  public static final String MODULE_TYPE_ID = "UID#1";
  public static final String MODULE_1 = "Module1";
  public static final String MODULE_2 = "Module2";

  @Test
  public void testGetProjectModuleNamesWithCorrectModulesReturnsValid() {
    //  given
    Project project = getProject();
    createAndAddModule(project, "module1", MODULE_TYPE_ID);
    //  modules are identified by name, so only one is actually created
    createAndAddModule(project, "module1", MODULE_TYPE_ID);
    createAndAddModule(project, "module2", MODULE_TYPE_ID);
    Module[] modules = ModuleManager.getInstance(project).getModules();
    assertEquals("Only one module named 'module1' is created.", 3, modules.length);

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    MainViewModelUpdater mainViewModelUpdater = new MainViewModelUpdater(mainViewModel,
        project, 1000L);

    //  when
    Set<String> projectModuleNames = mainViewModelUpdater.getProjectModuleNames();

    //  then
    assertEquals("There are 3 (three) module names listed.", 3, projectModuleNames.size());
  }

  @Test
  public void testGetProjectModuleNamesWithClosedProjectReturnsEmpty() {
    //  given
    Project project = getProject();
    Project spyProject = spy(project);
    when(spyProject.isOpen()).thenReturn(false);

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(spyProject);
    MainViewModelUpdater mainViewModelUpdater = new MainViewModelUpdater(mainViewModel,
        spyProject, 1000L);

    //  when
    Set<String> projectModules = mainViewModelUpdater.getProjectModuleNames();

    //  then
    assertEmpty("There are 0 (zero) module names listed for a closed project.", projectModules);
  }

  @Test
  public void testGetUpdatableModulesWithNullCourseReturnEmpty() {
    //  given
    Project project = getProject();
    createAndAddModule(project, MODULE_1, MODULE_TYPE_ID);
    createAndAddModule(project, MODULE_2, MODULE_TYPE_ID);

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    MainViewModelUpdater mainViewModelUpdater = new MainViewModelUpdater(mainViewModel,
        project, 1000L);

    //  when
    List<fi.aalto.cs.apluscourses.model.Module> updatableModules = mainViewModelUpdater
        .getUpdatableModules(null);

    //  then
    assertEmpty("There are no modules for an empty course.", updatableModules);
  }

  /**
   * A helper method to create a simple {@link Course} with two {@link
   * fi.aalto.cs.apluscourses.model.Module}s.
   *
   * @return a {@link Course}
   * @throws MalformedURLException if the {@link URL} has incorrect format.
   */
  @NotNull
  public static Course getDummyCourseWithTwoModules() throws MalformedURLException {
    return getDummyCourseWithTwoModules(MODULE_1, MODULE_2);
  }

  /**
   * Same as {@link this#getDummyCourseWithTwoModules()} but takes {@link
   * fi.aalto.cs.apluscourses.model.Module} names as parameters.
   *
   * @return a {@link Course}
   * @throws MalformedURLException if the {@link URL} has incorrect format.
   */
  @NotNull
  public static Course getDummyCourseWithTwoModules(String firstModuleName, String secondModuleName)
      throws MalformedURLException {
    fi.aalto.cs.apluscourses.model.Module module1 = new ModelExtensions.TestModule(firstModuleName);
    fi.aalto.cs.apluscourses.model.Module module2 = new ModelExtensions.TestModule(
        secondModuleName);
    List<fi.aalto.cs.apluscourses.model.Module> modules = Arrays.asList(module1, module2);
    Map<String, String> requiredPlugins = new HashMap<>();
    requiredPlugins.put("org.intellij.awesome_plugin", "Awesome Plugin");
    Map<String, URL> resourceUrls = new HashMap<>();
    resourceUrls.put("key", new URL("http://localhost:8000"));
    return new Course("Tester Course", modules, Collections.emptyList(),
        requiredPlugins, resourceUrls);
  }
}