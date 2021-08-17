package fi.aalto.cs.apluscourses;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.ReplConfigurationFormModel;
import fi.aalto.cs.apluscourses.ui.repl.ReplConfigurationForm;
import org.jetbrains.annotations.NotNull;

/**
 * A helper interface< to simplify testing plugin manipulation logics.
 */
public interface TestHelper {

  /**
   * A helper method to ensure at least one (first) module for the default testing project is of a
   * Scala type.
   *
   * @param project a default testing {@link Project}
   */
  static void makeFirstPluginScalaModule(@NotNull Project project) {
    Module[] modules = ModuleManager.getInstance(project).getModules();
    makeFirstPluginScalaModule(modules);
  }

  /**
   * A helper method to ensure at least one (first) module for the default testing project is of a
   * Scala type.
   *
   * @param modules an array of {@link Module}s
   */
  static void makeFirstPluginScalaModule(@NotNull Module[] modules) {
    if (modules[0] != null) {
      Module module = modules[0];
      module.setModuleType("JAVA_MODULE");
    }
  }

  /**
   * A helper method to create a dummy {@link ReplConfigurationFormModel} from default project.
   *
   * @return the created {@link ReplConfigurationFormModel}
   */
  @NotNull
  default ReplConfigurationFormModel getDummyReplConfigurationFormModel(@NotNull Project project) {
    String workDir = project.getProjectFilePath();
    String moduleName = "light_idea_test_case";
    makeFirstPluginScalaModule(project);
    return new ReplConfigurationFormModel(project, workDir, moduleName);
  }

  /**
   * A helper method to create a dummy {@link ReplConfigurationForm} from default project.
   *
   * @return the created {@link ReplConfigurationForm}
   */
  @NotNull
  default ReplConfigurationForm getDummyReplConfigurationForm(@NotNull Project project) {
    return new ReplConfigurationForm(getDummyReplConfigurationFormModel(project), project);
  }
}
