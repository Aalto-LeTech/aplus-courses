package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import java.util.List;
import org.junit.Test;

public class ReplConfigurationFormModelTest extends BasePlatformTestCase implements TestHelper {

  @Test
  public void testGetScalaModuleNamesWithNonJavaModuleReturnsEmpty() {
    //  given
    Project project = getProject();
    Module[] modules = ModuleManager.getInstance(project).getModules();

    //  when
    List<String> moduleNames = ReplConfigurationFormModel.getScalaModuleNames(modules);

    //  then
    assertEmpty(moduleNames);
  }

  @Test
  public void testGetScalaModuleNamesWithJavaModuleReturnsModule() {
    //  given
    Project project = getProject();
    //  Default project has only one module called:
    String moduleName = "light_idea_test_case";
    Module[] modules = ModuleManager.getInstance(project).getModules();
    TestHelper.makeFirstPluginScalaModule(modules);

    //  when
    List<String> moduleNames = ReplConfigurationFormModel.getScalaModuleNames(modules);

    //  then
    assertEquals("Resulting filtered list contains the name of the module passed.",
        moduleName, moduleNames.get(0));
  }

  @Test
  public void testGetScalaModuleNamesWithNullTypedModuleReturnEmpty() {
    //  given
    Project project = getProject();
    Module[] modules = ModuleManager.getInstance(project).getModules();
    Module module = modules[0];
    // well, @NotNull is not always safe :D
    String nullString = "nullString";
    module.setModuleType(nullString);
    nullString = null;  // NOSONAR

    //  when
    List<String> moduleNames = ReplConfigurationFormModel.getScalaModuleNames(modules);

    //  then
    assertEmpty(moduleNames);
  }
}