package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.ui.IntelliJDialogs;
import fi.aalto.cs.apluscourses.ui.base.Dialogs;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;

public class ImportProjectSettingsAction extends AnAction {
  @NotNull
  private MainViewModelProvider mainViewModelProvider;

  @NotNull ProjectSettingsImporter projectSettingsImporter;

  @NotNull
  private Dialogs dialogs;

  /**
   * Construct an action with the given main view model provider, project settings importer and
   * dialog helper.
   */
  public ImportProjectSettingsAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                     @NotNull ProjectSettingsImporter projectSettingsImporter,
                                     @NotNull Dialogs dialogs) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.projectSettingsImporter = projectSettingsImporter;
    this.dialogs = dialogs;
  }

  public ImportProjectSettingsAction() {
    this(PluginSettings.getInstance(), ImportProjectSettingsAction::doImport,
        new IntelliJDialogs());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      informNoProjectOpen();
      return;
    }

    CourseViewModel courseViewModel = mainViewModelProvider
        .getMainViewModel(project)
        .courseViewModel
        .get();
    if (courseViewModel == null) {
      return;
    }
    Course course = courseViewModel.getModel();

    URL settingsUrl = course.getResourceUrls().get("projectSettings");
    if (settingsUrl == null) {
      informNoSettings(course);
      return;
    }

    try {
      projectSettingsImporter.doImport(project, settingsUrl);
    } catch (IOException | UnexpectedResponseException ex) {
      informErrorOccurred();
    }
  }

  /**
   * Gets the file names (excluding directories) from the given ZIP file.
   */
  @NotNull
  private static List<String> getFileNames(@NotNull ZipFile zipFile) throws IOException {
    return zipFile
        .getFileHeaders()
        .stream()
        .filter(file -> !file.isDirectory())
        .map(FileHeader::getFileName)
        .collect(Collectors.toList());
  }

  /**
   * Downloads the project settings ZIP file from the given URL to a temporary file. After that the
   * files from the .idea directory of the ZIP file are extracted to the .idea directory of the
   * given project, after which the project is reloaded.
   */
  private static void doImport(@NotNull Project project, @NotNull URL settingsUrl)
      throws IOException, UnexpectedResponseException {
    Path settingsPath = Paths.get(project.getBasePath(), Project.DIRECTORY_STORE_FOLDER);

    File settingsZip = FileUtilRt.createTempFile(project.getName() + "-settings", ".zip");
    CoursesClient.fetchZip(settingsUrl, settingsZip);
    ZipFile zipFile = new ZipFile(settingsZip);
    List<String> fileNames = getFileNames(zipFile);

    for (String fileName : fileNames) {
      Path path = Paths.get(fileName);
      Path pathWithoutRoot = path.subpath(1, path.getNameCount());
      zipFile.extractFile(path.toString(), settingsPath.toString(), pathWithoutRoot.toString());
    }

    ProjectManager.getInstance().reloadProject(project);
  }

  @FunctionalInterface
  public interface ProjectSettingsImporter {
    void doImport(Project project, URL settingsUrl) throws IOException, UnexpectedResponseException;
  }

  private void informNoProjectOpen() {
    dialogs.showInformationDialog("A project must be loaded before project settings are imported.",
        "No Project Currently Open");
  }

  private void informNoSettings(@NotNull Course course) {
    dialogs.showInformationDialog(
        "The course \"" + course.getName() + "\" does not provide custom project settings",
        "No Project Settings Found");
  }

  private void informErrorOccurred() {
    dialogs.showErrorDialog("An error occurred while importing project settings. Please check "
        + "your network connection and try again, or contact the course staff if the issue "
        + "persists.", "Import Project Settings");
  }
}
