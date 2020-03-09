package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.ProjectTopics;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class IntelliJModelFactory implements ModelFactory {

  @NotNull
  private final Project project;

  public IntelliJModelFactory(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public Course createCourse(@NotNull String name,
                             @NotNull List<Module> modules,
                             @NotNull Map<String, String> requiredPlugins) {
    IntelliJCourse course = new IntelliJCourse(name, modules, requiredPlugins, project);
    // Add a module change listener with the created course instance to the project
    project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
      @Override
      public void moduleRemoved(@NotNull Project project,
                                @NotNull com.intellij.openapi.module.Module projectModule) {
          course
              .getModuleOptional(projectModule.getName())
              .ifPresent(module -> course.onModuleRemove(module));
      }
    });
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,
        new BulkFileListener() {
          @Override
          public void after(@NotNull List<? extends VFileEvent> events) {
            for (VFileEvent event : events) {
              if (event instanceof VFileDeleteEvent) {
                String deletedFileName = event.getFile().getName();
                course
                    .getModuleOptional(deletedFileName)
                    .ifPresent(module -> course.onModuleFilesDeletion(module));
              }
            }
          }
        }
    );
    return course;
  }

  @Override
  public Module createModule(@NotNull String name, @NotNull URL url) {
    Module module = new IntelliJModule(name, url, project);
    // IntelliJ modules may already be present in the project or file system, so we update the state
    // at module creation here.
    module.updateState();
    return module;
  }
}
