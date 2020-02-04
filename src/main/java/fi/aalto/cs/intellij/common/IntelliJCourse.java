package fi.aalto.cs.intellij.common;

import com.intellij.openapi.project.Project;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class IntelliJCourse extends Course {
  @NotNull
  private final Project project;

  public IntelliJCourse(@NotNull String name,
                        @NotNull List<Module> modules,
                        @NotNull Map<String, String> requiredPlugins,
                        @NotNull Project project) {
    super(name, modules, requiredPlugins);

    this.project = project;
  }
}
