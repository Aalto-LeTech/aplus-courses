package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Callbacks;
import fi.aalto.cs.apluscourses.utils.CourseHiddenElements;
import fi.aalto.cs.apluscourses.utils.PluginDependency;
import fi.aalto.cs.apluscourses.utils.Version;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModelFactory {

  Course createCourse(@NotNull String id,
                      @NotNull String name,
                      @NotNull String aplusUrl,
                      @NotNull List<String> languages,
                      @NotNull List<Module> modules,
                      @NotNull List<Library> libraries,
                      @NotNull Map<Long, Map<String, String>> exerciseModules,
                      @NotNull Map<String, URL> resourceUrls,
                      @NotNull Map<String, String> vmOptions,
                      @NotNull Set<String> optionalCategories,
                      @NotNull List<String> autoInstallComponentNames,
                      @NotNull Map<String, String[]> replInitialCommands,
                      @NotNull String replAdditionalArguments,
                      @NotNull Version courseVersion,
                      @NotNull List<PluginDependency> pluginDependencies,
                      @NotNull CourseHiddenElements hiddenElements,
                      @NotNull Callbacks callbacks,
                      boolean requireAuthenticationForModules,
                      @Nullable String feedbackParser,
                      @NotNull String newsParser,
                      long courseLastModified);

  Module createModule(@NotNull String name, @NotNull URL url, @NotNull Version version,
                      @NotNull String changelog, boolean isSbtModule);

  Library createLibrary(@NotNull String name);
}
