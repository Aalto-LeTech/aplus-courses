package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.ProjectTopics;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentInitializationCallback;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntelliJModelFactory implements ModelFactory {

  private static final Logger logger = LoggerFactory.getLogger(IntelliJModelFactory.class);

  @NotNull
  private final APlusProject project;

  public IntelliJModelFactory(@NotNull Project project) {
    this.project = new APlusProject(project);
  }

  @Override
  public Course createCourse(@NotNull String name,
      @NotNull List<Module> modules,
      @NotNull List<Library> libraries,
      @NotNull Map<String, String> requiredPlugins,
      @NotNull Map<String, URL> resourceUrls) {

    IntelliJCourse course =
        new IntelliJCourse(name, modules, libraries, requiredPlugins, resourceUrls, project);

    ComponentInitializationCallback componentInitializationCallback =
        component -> registerComponentToCourse(component, course);
    course.commonLibraryProvider.setInitializationCallback(componentInitializationCallback);

    project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
      @Override
      public void moduleRemoved(@NotNull Project project,
          @NotNull com.intellij.openapi.module.Module projectModule) {
        Optional.of(projectModule.getName())
            .map(course::getComponentIfExists)
            .ifPresent(Component::setUnresolved);
      }
    });
    project.getLibraryTable().addListener(new LibraryTable.Listener() {
      @Override
      public void afterLibraryRemoved(
          @NotNull com.intellij.openapi.roots.libraries.Library library) {
        Optional.ofNullable(library.getName())
            .map(course::getComponentIfExists)
            .ifPresent(Component::setUnresolved);
      }
    });
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,
        new BulkFileListener() {
          @Override
          public void after(@NotNull List<? extends VFileEvent> events) {
            for (VFileEvent event : events) {
              if (event instanceof VFileDeleteEvent) {
                Optional.ofNullable(event.getFile())
                    .map(course::getComponentIfExists)
                    .ifPresent(Component::setUnresolved);
              }
            }
          }
        }
    );

    course.getComponents().forEach(componentInitializationCallback::initialize);
    course.resolve();

    return course;
  }

  private void registerComponentToCourse(@NotNull Component component, @NotNull Course course) {
    component.onError.addListener(course, Course::resolve);
  }

  @Override
  public Module createModule(@NotNull String name, @NotNull URL url) {
    // IntelliJ modules may already be present in the project or file system, so we determine the
    // state at module creation here.
    return new IntelliJModule(name, url, project);
  }

  @Override
  public Library createLibrary(@NotNull String name) {
    throw new UnsupportedOperationException(
        "Only common libraries like Scala SDK are currently supported.");
  }
}
