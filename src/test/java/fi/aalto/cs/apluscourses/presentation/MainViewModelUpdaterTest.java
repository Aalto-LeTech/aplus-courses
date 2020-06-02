package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.HeavyPlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.intellij.model.APlusProject;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import java.io.IOException;
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
  public void testAddSameModuleTwiceFails() throws MalformedURLException {
    //  given, when, then, whatever
    Course course = getDummyCourse();
    /**
     *  Check {@link MainViewModelUpdaterTest#getDummyCourse()} method.
     **/
    fi.aalto.cs.apluscourses.model.Module module2 = new ModelExtensions.TestModule("Module2");
    assertThrows(UnsupportedOperationException.class, () -> course.getModules().add(module2));
  }

  @Test
  public void testGetUpdatableModulesWithNullCourseReturnEmpty() {
    //  given
    Project project = getProject();
    createAndAddModule(project, "Module1", "UID#1");
    createAndAddModule(project, "Module2", "UID#1");

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    MainViewModelUpdater mainViewModelUpdater = new MainViewModelUpdater(mainViewModel,
        project, 1000L);

    //  when
    List<fi.aalto.cs.apluscourses.model.Module> updatableModules = mainViewModelUpdater
        .getUpdatableModules(null);

    //  then
    assertEmpty("There are no modules for an empty course.", updatableModules);
  }

  @Test
  public void testGetUpdatableModulesWithGetCourseFileModuleIdsThrowingReturnsEmpty()
      throws IOException {
    //  given
    Project project = getProject();
    createAndAddModule(project, "Module1", "UID#1");
    createAndAddModule(project, "Module2", "UID#1");

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    MainViewModelUpdater mainViewModelUpdater = new MainViewModelUpdater(mainViewModel,
        project, 1000L);

    MainViewModelUpdater spyMainViewModelUpdater = spy(mainViewModelUpdater);
    APlusProject mockAplusProject = mock(APlusProject.class);
    when(spyMainViewModelUpdater.getAplusProject()).thenReturn(mockAplusProject);
    when(mockAplusProject.getCourseFileModuleIds()).thenThrow(new IOException());

    Course course = getDummyCourse();

    //  when
    List<fi.aalto.cs.apluscourses.model.Module> updatableModules = spyMainViewModelUpdater
        .getUpdatableModules(course);

    //  then
    assertEmpty("There are no modules for an empty course.", updatableModules);
  }

  @NotNull
  private Course getDummyCourse() throws MalformedURLException {
    fi.aalto.cs.apluscourses.model.Module module1 = new ModelExtensions.TestModule("Module1");
    fi.aalto.cs.apluscourses.model.Module module2 = new ModelExtensions.TestModule("Module2");
    List<fi.aalto.cs.apluscourses.model.Module> modules = Arrays.asList(module1, module2);
    Map<String, String> requiredPlugins = new HashMap<>();
    requiredPlugins.put("org.intellij.awesome_plugin", "Awesome Plugin");
    Map<String, URL> resourceUrls = new HashMap<>();
    resourceUrls.put("key", new URL("http://localhost:8000"));
    return new Course("Tester Course", modules, Collections.emptyList(),
        requiredPlugins, resourceUrls);
  }
}