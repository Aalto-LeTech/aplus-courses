package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.Map;

public interface ModelFactory {

  Course createCourse(@NotNull String id,
                      @NotNull String name,
                      @NotNull String aplusUrl,
                      @NotNull List<String> languages,
                      @NotNull List<Module> modules,
                      @NotNull List<Library> libraries,
                      @NotNull Map<Long, Map<String, String>> exerciseModules,
                      @NotNull Map<String, URL> resourceUrls,
                      @NotNull List<String> autoInstallComponentNames,
                      @NotNull Map<String, String[]> replInitialCommands);

  Module createModule(@NotNull String name, @NotNull URL url, @NotNull String versionId);

  Library createLibrary(@NotNull String name);
}
