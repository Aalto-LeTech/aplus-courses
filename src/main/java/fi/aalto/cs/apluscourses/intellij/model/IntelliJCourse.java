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
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.dal.APlusExerciseDataSource;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.Tutorial;
import fi.aalto.cs.apluscourses.utils.PluginDependency;
import fi.aalto.cs.apluscourses.utils.Version;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJCourse extends Course {

  @NotNull
  private final APlusProject project;

  private final CommonLibraryProvider commonLibraryProvider;

  private final PlatformListener platformListener;

  private final ExerciseDataSource exerciseDataSource;

  /**
   * Constructor.
   */
  public IntelliJCourse(@NotNull String id,
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
                        @NotNull APlusProject project,
                        @NotNull CommonLibraryProvider commonLibraryProvider,
                        @NotNull Map<Long, Tutorial> tutorials,
                        @NotNull List<PluginDependency> pluginDependencies,
                        @Nullable String feedbackParser,
                        @Nullable String newsParser,
                        long courseLastModified) {
    super(
        id,
        name,
        aplusUrl,
        languages,
        modules,
        libraries,
        exerciseModules,
        resourceUrls,
        vmOptions,
        optionalCategories,
        autoInstallComponentNames,
        replInitialCommands,
        replAdditionalArguments,
        courseVersion,
        tutorials,
        pluginDependencies,
        feedbackParser,
        newsParser
    );

    this.project = project;
    this.commonLibraryProvider = commonLibraryProvider;
    this.platformListener = new PlatformListener();
    this.exerciseDataSource = new APlusExerciseDataSource(getApiUrl(), project.getBasePath()
        .resolve(Paths.get(Project.DIRECTORY_STORE_FOLDER, "a-plus-cache.json")), courseLastModified);
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
    return component != null && component.getFullPath().equals(Paths.get(file.getPath())) ? component : null;
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

    @RequiresReadLock
    public synchronized void registerListeners() {
      if (messageBusConnection != null) {
        return;
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

    @RequiresReadLock
    public synchronized void unregisterListeners() {
      if (messageBusConnection == null) {
        return;
      }
      messageBusConnection.disconnect();
      messageBusConnection = null;
      project.getLibraryTable().removeListener(libraryTableListener);
    }
  }
}
