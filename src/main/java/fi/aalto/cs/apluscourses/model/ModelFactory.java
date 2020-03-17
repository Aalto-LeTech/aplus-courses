package fi.aalto.cs.apluscourses.model;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface ModelFactory {
  Course createCourse(@NotNull String name,
                      @NotNull List<Module> modules,
                      @NotNull List<Library> libraries,
                      @NotNull Map<String, String> requiredPlugins);

  Module createModule(@NotNull String name, @NotNull URL url);

  Library createLibrary(@NotNull String name);
}
