package fi.aalto.cs.intellij.model;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface CourseFactory {
  Course createCourse(@NotNull String name, @NotNull List<Module> modules,
                      @NotNull Map<String, String> requiredPlugins);

  Module createModule(@NotNull String name, @NotNull URL url);
}
