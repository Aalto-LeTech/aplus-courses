package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectModuleSource {

  @NotNull
  public Module[] getModules(@NotNull Project project) {
    return ModuleManager.getInstance(project).getModules();
  }

  @Nullable
  public Module getModule(@NotNull Project project, @NotNull String moduleName) {
    return ModuleManager.getInstance(project).findModuleByName(moduleName);
  }

}
