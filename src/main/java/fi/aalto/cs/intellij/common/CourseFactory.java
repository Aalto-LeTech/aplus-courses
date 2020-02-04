package fi.aalto.cs.intellij.common;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface CourseFactory {
  /**
   * For test purposes only.
   */
  CourseFactory DEFAULT = new CourseFactory() {
    @Override
    public Course createCourse(@NotNull String name, @NotNull List<Module> modules, @NotNull Map<String, String> requiredPlugins) {
      return new Course(name, modules, requiredPlugins);
    }

    @Override
    public Module createModule(@NotNull String name, @NotNull URL url) {
      return new Module(name, url);
    }
  };

  Course createCourse(@NotNull String name, @NotNull List<Module> modules,
                      @NotNull Map<String, String> requiredPlugins);

  Module createModule(@NotNull String name, @NotNull URL url);
}
