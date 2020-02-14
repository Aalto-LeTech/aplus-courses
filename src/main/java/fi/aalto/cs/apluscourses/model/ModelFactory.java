package fi.aalto.cs.apluscourses.model;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface ModelFactory {
  default Course createCourse(@NotNull String name, @NotNull List<Module> modules,
                      @NotNull Map<String, String> requiredPlugins) {
    return new Course(name, modules, requiredPlugins);
  }

  default Module createModule(@NotNull String name, @NotNull URL url) {
    return new Module(name, url);
  }
}
