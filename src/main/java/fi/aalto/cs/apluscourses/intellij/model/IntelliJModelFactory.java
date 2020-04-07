package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import fi.aalto.cs.apluscourses.intellij.utils.RequiredPluginsCheckerUtil;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleLoadException;
import fi.aalto.cs.apluscourses.model.NoSuchModuleException;
import fi.aalto.cs.apluscourses.utils.StateMonitor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntelliJModelFactory implements ModelFactory {

  private static final Logger logger = LoggerFactory.getLogger(IntelliJModelFactory.class);

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
        Module module = course.getModuleOpt(projectModule.getName());
        if (module != null) {
          markDependentModulesInvalid(course, module.getName());
          course.onModuleRemove(module);
        }
      }
    });
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,
        new BulkFileListener() {
          @Override
          public void after(@NotNull List<? extends VFileEvent> events) {
            for (VFileEvent event : events) {
              if (event instanceof VFileDeleteEvent) {
                String deletedFileName = event.getFile().getName();
                Module module = course.getModuleOpt(deletedFileName);
                if (module != null) {
                  course.onModuleFilesDeletion(module);
                }
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

  private void markDependentModulesInvalid(@NotNull IntelliJCourse course,
      @NotNull String removedModuleName) {
    Project project = course.getProject();
    com.intellij.openapi.module.Module[] projectModules = ModuleManager.getInstance(project)
        .getModules();

    for (com.intellij.openapi.module.Module module : projectModules) {
      Module courseModule = getCourseModule(course, module);
      if (courseModule != null) {
        List<String> courseModuleDependencies = getCourseModuleDependencies(courseModule);
        if (courseModuleDependencies != null
            && courseModuleDependencies.contains(removedModuleName)) {
          courseModule.stateMonitor.set(StateMonitor.ERROR);
        }
      }
    }
  }

  @Nullable
  private Module getCourseModule(@NotNull IntelliJCourse course,
      @NotNull com.intellij.openapi.module.Module module) {
    Module courseModule = null;
    try {
      courseModule = course.getModule(module.getName());
    } catch (NoSuchModuleException e) {
      logger.error(e.getMessage(), e);
    }
    return courseModule;
  }

  @Nullable
  private List<String> getCourseModuleDependencies(@NotNull Module courseModule) {
    List<String> courseModuleDependencies = null;
    try {
      courseModuleDependencies = courseModule.getDependencies();
    } catch (ModuleLoadException e) {
      logger.error(e.getMessage(), e);
    }
    return courseModuleDependencies;
  }
}
