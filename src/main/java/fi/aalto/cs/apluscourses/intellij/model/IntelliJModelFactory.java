package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.ProjectTopics;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import fi.aalto.cs.apluscourses.model.ComponentLoadException;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.StateMonitor;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    // Add a module change listener with the created course instance to the project
    // TODO: These should be handled in those classes; also, renaming issue?
    project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
      @Override
      public void moduleRemoved(@NotNull Project project,
          @NotNull com.intellij.openapi.module.Module projectModule) {
        markDependentModulesInvalid(course, projectModule.getName());
        course.onComponentRemove(course.getComponentIfExists(projectModule.getName()));
      }
    });
    project.getLibraryTable().addListener(new LibraryTable.Listener() {
      @Override
      public void afterLibraryRemoved(
          @NotNull com.intellij.openapi.roots.libraries.Library library) {
        course.onComponentRemove(course.getComponentIfExists(name));
      }
    });
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,
        new BulkFileListener() {
          @Override
          public void after(@NotNull List<? extends VFileEvent> events) {
            for (VFileEvent event : events) {
              if (event instanceof VFileDeleteEvent) {
                VirtualFile deletedFile = Objects.requireNonNull(event.getFile());
                course.onComponentFilesDeleted(course.getComponentIfExists(deletedFile));
              }
            }
          }
        }
    );
    return course;
  }

  @Override
  public Module createModule(@NotNull String name, @NotNull URL url) {
    // IntelliJ modules may already be present in the project or file system, so we determine the
    // state at module creation here.
    return new IntelliJModule(name, url, project, project.resolveModuleState(name));
  }

  @Override
  public Library createLibrary(@NotNull String name) {
    throw new UnsupportedOperationException(
        "Only common libraries like Scala SDK are currently supported.");
  }

  /**
   * This buddy here does all the nasty heavy lifting in order to check whether there are {@link
   * Module} that depend on the removed {@link com.intellij.openapi.module.Module} and if finds,
   * marks their {@link Module}s {@link StateMonitor} state.
   *
   * @param course            a {@link Course} which {@link Module}s to check
   * @param removedModuleName a {@link String} name of the removed
   * {@link com.intellij.openapi.module.Module}
   */
  public void markDependentModulesInvalid(@NotNull IntelliJCourse course,
      @NotNull String removedModuleName) {
    APlusProject project = course.getProject();
    com.intellij.openapi.module.Module[] projectModules = project.getModuleManager().getModules();

    for (com.intellij.openapi.module.Module module : projectModules) {
      Module courseModule = (Module) course.getComponentIfExists(module.getName());
      if (courseModule != null) {
        List<String> courseModuleDependencies = getCourseModuleDependencies(courseModule);
        if (courseModuleDependencies != null && !courseModuleDependencies.isEmpty()
            && courseModuleDependencies.contains(removedModuleName)) {
          courseModule.stateMonitor.set(StateMonitor.ERROR);
        }
      }
    }
  }

  /**
   * A simple wrapper method on {@link Module#getDependencies()} to hide the thrown {@link
   * ComponentLoadException}.
   *
   * @param courseModule a {@link Module} to get dependencies from
   * @return a {@link List} of {@link String}s representing names of the dependencies.
   */
  @Nullable
  public List<String> getCourseModuleDependencies(@NotNull Module courseModule) {
    List<String> courseModuleDependencies = null;
    try {
      courseModuleDependencies = courseModule.getDependencies();
    } catch (ComponentLoadException ex) {
      logger.error(ex.getMessage(), ex);
    }
    return courseModuleDependencies;
  }
}
