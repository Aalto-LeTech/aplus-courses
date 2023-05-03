package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleMetadata;
import fi.aalto.cs.apluscourses.model.Tutorial;
import fi.aalto.cs.apluscourses.utils.CourseHiddenElements;
import fi.aalto.cs.apluscourses.utils.PluginDependency;
import fi.aalto.cs.apluscourses.utils.Version;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJModelFactory implements ModelFactory {

  @NotNull
  private final APlusProject project;

  private final Map<String, ModuleMetadata> modulesMetadata;


  /**
   * Construct a factory instance with the given project.
   */
  public IntelliJModelFactory(@NotNull Project project) {
    this.project = new APlusProject(project);
    modulesMetadata = PluginSettings
        .getInstance()
        .getCourseFileManager(project)
        .getModulesMetadata();
  }

  @Override
  public Course createCourse(@NotNull String id,
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
                             @NotNull Map<Long, Tutorial> tutorials,
                             @NotNull List<PluginDependency> pluginDependencies,
                             @NotNull CourseHiddenElements hiddenElements,
                             @Nullable String feedbackParser,
                             @Nullable String newsParser,
                             long courseLastModified) {

    IntelliJCourse course =
        new IntelliJCourse(id, name, aplusUrl, languages, modules, libraries, exerciseModules,
            resourceUrls, vmOptions, optionalCategories, autoInstallComponentNames, replInitialCommands,
            replAdditionalArguments, courseVersion, project, new CommonLibraryProvider(project), tutorials,
            pluginDependencies, hiddenElements, feedbackParser, newsParser, courseLastModified);

    Component.InitializationCallback componentInitializationCallback =
        component -> registerComponentToCourse(component, course);
    course.getCommonLibraryProvider().setInitializationCallback(componentInitializationCallback);
    course.getComponents().forEach(componentInitializationCallback::initialize);

    course.resolve();

    return course;
  }

  private void registerComponentToCourse(@NotNull Component component, @NotNull Course course) {
    component.onError.addListener(course, Course::resolve);
  }

  @Override
  public Module createModule(@NotNull String name,
                             @NotNull URL url,
                             @NotNull Version version,
                             @NotNull String changelog) {
    ModuleMetadata moduleMetadata = Optional.ofNullable(modulesMetadata.get(name))
        .orElse(new ModuleMetadata(null, null));
    return new IntelliJModule(name, url, changelog, version, moduleMetadata.getVersion(),
        moduleMetadata.getDownloadedAt(), project);
  }

  @Override
  public Library createLibrary(@NotNull String name) {
    throw new UnsupportedOperationException(
        "Only common libraries like Scala SDK are currently supported.");
  }
}
