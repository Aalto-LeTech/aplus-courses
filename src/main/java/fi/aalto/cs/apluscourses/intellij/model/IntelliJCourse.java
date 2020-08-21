package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.ProjectTopics;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.dal.APlusExerciseDataSource;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.Module;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.CalledWithReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class IntelliJCourse extends Course {

  @NotNull
  private final APlusProject project;

  private final CommonLibraryProvider commonLibraryProvider;

  private final PlatformListener platformListener;

  private final ExerciseDataSource exerciseDataSource;

  public IntelliJCourse(@NotNull String id,
                        @NotNull String name,
                        @NotNull String aplusUrl,
                        @NotNull List<Module> modules,
                        @NotNull List<Library> libraries,
                        @NotNull Map<Long, Map<String, String>> exerciseModules,
                        @NotNull Map<String, URL> resourceUrls,
                        @NotNull List<String> autoInstallComponentNames,
                        @NotNull Map<String, String[]> replInitialCommands,
                        @NotNull APlusProject project,
                        @NotNull CommonLibraryProvider commonLibraryProvider) {
    super(
        id,
        name,
        aplusUrl,
        modules,
        libraries,
        exerciseModules,
        resourceUrls,
        autoInstallComponentNames,
        replInitialCommands);

    this.project = project;
    this.commonLibraryProvider = commonLibraryProvider;
    this.platformListener = new PlatformListener();
    this.exerciseDataSource = new APlusExerciseDataSource(getApiUrl());
  }

  @NotNull
  public APlusProject getProject() {
    return project;
  }

  @NotNull
  public CommonLibraryProvider getCommonLibraryProvider() {
    return commonLibraryProvider;
  }

  @Nullable
  @Override
  public Component getComponentIfExists(@NotNull String name) {
    Component component = super.getComponentIfExists(name);
    return component != null ? component : commonLibraryProvider.getComponentIfExists(name);
  }

  @Nullable
  public Component getComponentIfExists(VirtualFile file) {
    Component component = getComponentIfExists(file.getName());
    Path pathOfTheFile = Paths.get(file.getPath());
    return component != null && component.getFullPath().equals(pathOfTheFile) ? component : null;
  }

  @NotNull
  @Override
  public Collection<Component> getComponents() {
    return Stream.concat(
        super.getComponents().stream(),
        commonLibraryProvider.getProvidedLibraries().stream()
    ).collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public void register() {
    ReadAction.run(platformListener::registerListeners);
  }

  @Override
  public void unregister() {
    ReadAction.run(platformListener::unregisterListeners);

  }

  @NotNull
  @Override
  public ExerciseDataSource getExerciseDataSource() {
    return exerciseDataSource;
  }

  private class PlatformListener {

    private MessageBusConnection messageBusConnection;

    @NotNull
    private final LibraryTable.Listener libraryTableListener = new LibraryTable.Listener() {
      @Override
      public void afterLibraryRemoved(
          @NotNull com.intellij.openapi.roots.libraries.Library library) {
        Optional.ofNullable(library.getName())
            .map(IntelliJCourse.this::getComponentIfExists)
            .ifPresent(Component::setUnresolved);
      }
    };

    @CalledWithReadLock
    public synchronized void registerListeners() {
      if (messageBusConnection != null) {
        throw new IllegalStateException();
      }
      messageBusConnection = project.getMessageBus().connect();
      messageBusConnection.subscribe(VirtualFileManager.VFS_CHANGES,
          new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
              for (VFileEvent event : events) {
                if (event instanceof VFileDeleteEvent) {
                  Optional.ofNullable(event.getFile())
                      .map(IntelliJCourse.this::getComponentIfExists)
                      .ifPresent(Component::setUnresolved);
                }
              }
            }
          }
      );
      messageBusConnection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
        @Override
        public void moduleRemoved(@NotNull Project project,
                                  @NotNull com.intellij.openapi.module.Module projectModule) {
          Optional.of(projectModule.getName())
              .map(IntelliJCourse.this::getComponentIfExists)
              .ifPresent(Component::setUnresolved);
        }
      });
      project.getLibraryTable().addListener(libraryTableListener);
    }

    @CalledWithReadLock
    public synchronized void unregisterListeners() {
      if (messageBusConnection == null) {
        throw new IllegalStateException();
      }
      messageBusConnection.disconnect();
      messageBusConnection = null;
      project.getLibraryTable().removeListener(libraryTableListener);
    }
  }
}
