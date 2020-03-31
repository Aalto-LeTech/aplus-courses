package fi.aalto.cs.apluscourses;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
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
   * @param modules an array of {@link Modules}s
   */
  public static void makeFirstPluginScalaModule(@NotNull Module[] modules){
    if (modules[0] != null) {
      Module module = modules[0];
      module.setModuleType("JAVA_MODULE");
    }
  }
}
