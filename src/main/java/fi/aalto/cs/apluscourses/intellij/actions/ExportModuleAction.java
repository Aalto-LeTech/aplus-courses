package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.intellij.model.ProjectModuleSource;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.IoErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.presentation.FileSaveViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import java.io.IOException;
import java.nio.file.Path;
import net.lingala.zip4j.ZipFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExportModuleAction extends AnAction {

  @NotNull
  private final ProjectModuleSource moduleSource;

  @NotNull
  private final ProjectPathResolver projectPathResolver;

  @NotNull
  private final DirectoryZipper zipper;

  @NotNull
  private final Dialogs dialogs;

  @NotNull
  private final Notifier notifier;

  /**
   * Create an {@link ExportModuleAction} with the given parameters. This constructor is mostly
   * useful for testing purposes.
   */
  public ExportModuleAction(@NotNull ProjectModuleSource moduleSource,
                            @NotNull ProjectPathResolver projectPathResolver,
                            @NotNull DirectoryZipper zipper,
                            @NotNull Dialogs dialogs,
                            @NotNull Notifier notifier) {
    this.moduleSource = moduleSource;
    this.projectPathResolver = projectPathResolver;
    this.zipper = zipper;
    this.dialogs = dialogs;
    this.notifier = notifier;
  }

  /**
   * Create an {@link ExportModuleAction} with sensible defaults.
   */
  public ExportModuleAction() {
    this(
        new ProjectModuleSource(),
        project -> project.getProjectFile().getParent().getParent(),
        (zip, directory) -> new ZipFile(zip.toString()).addFolder(directory.toFile()),
        Dialogs.DEFAULT,
        new DefaultNotifier()
    );
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    e.getPresentation().setEnabled(project != null && !project.isDefault());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null || project.isDefault()) {
      return;
    }

    Module[] availableModules = moduleSource.getModules(project);

    ModuleSelectionViewModel moduleSelectionViewModel = new ModuleSelectionViewModel(
        availableModules, getText("ui.exportModule.selectModule"), project);
    if (!dialogs.create(moduleSelectionViewModel, project).showAndGet()) {
      return;
    }

    Module selectedModule = moduleSelectionViewModel.selectedModule.get();
    VirtualFile moduleDirectory = moduleSelectionViewModel.selectedModuleFile.get();
    if (selectedModule == null || moduleDirectory == null) {
      return; // Should never happen though
    }

    FileSaveViewModel fileViewModel = new FileSaveViewModel(
        getText("ui.exportModule.saveDialog.title"),
        getText("ui.exportModule.saveDialog.description"),
        projectPathResolver.getProjectPath(project),
        selectedModule.getName() + ".zip"
    );

    if (!dialogs.create(fileViewModel, project).showAndGet()) {
      return;
    }

    try {
      zipper.zipDirectory(
          fileViewModel.getPath(), moduleDirectory.toNioPath()
      );
    } catch (IOException ex) {
      notifier.notify(new IoErrorNotification(ex), project);
    }
  }

  @FunctionalInterface
  public interface ProjectPathResolver {
    @Nullable
    VirtualFile getProjectPath(@NotNull Project project);
  }

  @FunctionalInterface
  public interface DirectoryZipper {
    void zipDirectory(@NotNull Path zipPath, @NotNull Path directoryPath) throws IOException;
  }

}
