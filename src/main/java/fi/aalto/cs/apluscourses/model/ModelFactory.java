package fi.aalto.cs.apluscourses.model;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface ModelFactory {
  Course createCourse(@NotNull String name,
                      @NotNull List<Module> modules,
                      @NotNull Map<String, String> requiredPlugins,
                      @NotNull Map<String, URL> resourceUrls);

  Module createModule(@NotNull String name, @NotNull URL url);
}
