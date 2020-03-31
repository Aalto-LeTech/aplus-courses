package fi.aalto.cs.apluscourses;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.presentation.ReplConfigurationFormModel;
import fi.aalto.cs.apluscourses.ui.repl.ReplConfigurationForm;
import org.jetbrains.annotations.NotNull;

public abstract class TestHelper extends BasePlatformTestCase {

  /**
   * A helper method to ensure at least one (first) module for the default testing project is of a
   * Scala type.
   *
   * @param project a default testing {@link Project}
   */
  public static void makeFirstPluginScalaModule(@NotNull Project project) {
    Module[] modules = ModuleManager.getInstance(project).getModules();
    makeFirstPluginScalaModule(modules);
  }

  /**
   * A helper method to ensure at least one (first) module for the default testing project is of a
   * Scala type.
   *
   * @param modules an array of {@link Module}s
   */
  public static void makeFirstPluginScalaModule(@NotNull Module[] modules){
    if (modules[0] != null) {
      Module module = modules[0];
      module.setModuleType("JAVA_MODULE");
    }
  }

  public ReplConfigurationFormModel getDummyReplConfigurationFormModel() {
    Project project = getProject();
    String workDir = project.getProjectFilePath();
    String moduleName = "light_idea_test_case";
    makeFirstPluginScalaModule(project);
    return new ReplConfigurationFormModel(project, workDir, moduleName);
  }

  public ReplConfigurationForm getDummyReplConfigurationForm(){
    return new ReplConfigurationForm(getDummyReplConfigurationFormModel());
  }
}
