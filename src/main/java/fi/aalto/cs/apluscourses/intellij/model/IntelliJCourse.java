package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.NoSuchComponentException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class IntelliJCourse extends Course {
  @NotNull
  private final APlusProject project;
  @NotNull
  private final Map<String, Component> components;
  @NotNull
  private final CommonLibraryProvider commonLibraryProvider;

  public IntelliJCourse(@NotNull String name,
                        @NotNull List<Module> modules,
                        @NotNull List<Library> libraries,
                        @NotNull Map<String, String> requiredPlugins,
                        @NotNull APlusProject project) {
    super(name, modules, libraries, requiredPlugins);

    this.project = project;
    this.commonLibraryProvider = new CommonLibraryProvider(project);
    this.components = Stream.concat(modules.stream(), libraries.stream())
        .map(IntelliJModule.class::cast)
        .collect(Collectors.toMap(IntelliJModule::getName, Function.identity()));
  }

  @NotNull
  public APlusProject getProject() {
    return project;
  }

  /**
   * Updates the states of the component objects when the given IntelliJ module or library is
   * removed from the IntelliJ project of this course. If the argument is null, does nothing.
   */
  public void onComponentRemove(@Nullable Component component) {
    if (component != null) {
      component.stateMonitor.set(Module.UNLOADED);
    }
  }

  /**
   * Updates the states of the component objects, when the directory with the files of the given
   * module or library is deleted. If the argument is null, does nothing.
   */
  public void onComponentFilesDeleted(@Nullable Component component) {
    if (component != null) {
      component.stateMonitor.set(Module.UNINSTALLED);
    }
  }

  @Nullable
  @Override
  public Component getComponentIfExists(@NotNull String name) {
    Component component = components.get(name);
    if (component != null) {
      return component;
    }
    return commonLibraryProvider.getComponentIfExists(name);
  }

  @NotNull
  @Override
  public Component getComponent(@NotNull String componentName) throws NoSuchComponentException {
    Component component = components.get(componentName);
    if (component != null) {
      return component;
    }
    return commonLibraryProvider.getComponent(componentName);
  }

  @Nullable
  public Component getComponentIfExists(VirtualFile file) {
    Component component = getComponentIfExists(file.getName());
    if (component != null && component.getPath().toString().equals(file.getPath())) {
      return component;
    }
    return null;
  }
}
