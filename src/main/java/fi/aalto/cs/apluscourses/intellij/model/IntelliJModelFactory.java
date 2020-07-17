package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.utils.CourseFileManager;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleMetadata;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class IntelliJModelFactory implements ModelFactory {

  @NotNull
  private final APlusProject project;

  private final Map<String, ModuleMetadata> modulesMetadata;


  public IntelliJModelFactory(@NotNull Project project) {
    this.project = new APlusProject(project);
    modulesMetadata = CourseFileManager.getInstance().getModulesMetadata();
  }

  @Override
  public Course createCourse(@NotNull String id,
                             @NotNull String name,
                             @NotNull List<Module> modules,
                             @NotNull List<Library> libraries,
                             @NotNull Map<String, String> requiredPlugins,
                             @NotNull Map<String, URL> resourceUrls,
                             @NotNull List<String> autoInstallComponentNames) {

    IntelliJCourse course =
        new IntelliJCourse(id, name, modules, libraries, requiredPlugins, resourceUrls,
            autoInstallComponentNames, project, new CommonLibraryProvider(project));

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
                             @NotNull String versionId,
                             @NotNull List<String> replInitialCommands) {
    ModuleMetadata moduleMetadata = Optional.ofNullable(modulesMetadata.get(name))
        .orElse(new ModuleMetadata(null, null));
    return new IntelliJModule(name, url, versionId, moduleMetadata.getModuleId(),
        moduleMetadata.getDownloadedAt(), replInitialCommands, project);
  }

  @Override
  public Library createLibrary(@NotNull String name) {
    throw new UnsupportedOperationException(
        "Only common libraries like Scala SDK are currently supported.");
  }
}
