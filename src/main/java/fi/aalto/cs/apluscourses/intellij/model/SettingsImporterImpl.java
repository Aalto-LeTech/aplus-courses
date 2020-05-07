package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.ide.startup.StartupActionScriptManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.updateSettings.impl.UpdateSettings;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;

public class SettingsImporterImpl implements SettingsImporter {


  /**
   * Downloads the course IDE settings ZIP file to a temporary file. Also adds IDEA startup actions
   * that unzip the temporary file to the IDEA configuration path after which the temporary file is
   * deleted. Therefore, the new IDE settings only take effect once IDEA is restarted and the
   * temporary file must still exist at that point.
   * @throws IOException                 If an IO error occurs (e.g. network issues).
   * @throws UnexpectedResponseException If an unexpected response is received when downloading the
   *                                     project settings. This usually indicates an error in the
   *                                     course configuration.
   */
  @Override
  public void importIdeSettings(@NotNull Course course)
      throws IOException, UnexpectedResponseException {
    URL ideSettingsUrl = course.getResourceUrls().get("ideSettings");
    if (ideSettingsUrl == null) {
      return;
    }

    File file = FileUtilRt.createTempFile("course-ide-settings", ".zip");
    CoursesClient.fetchZip(ideSettingsUrl, file);
    String configPath = FileUtilRt.toSystemIndependentName(PathManager.getConfigPath());
    StartupActionScriptManager.addActionCommands(
        Arrays.asList(
            new StartupActionScriptManager.UnzipCommand(file, new File(configPath)),
            new StartupActionScriptManager.DeleteCommand(file)
        )
    );

    UpdateSettings.getInstance().forceCheckForUpdateAfterRestart();

    PluginSettings.getInstance().setImportedIdeSettingsName(course.getName());
  }

  /**
   * Returns the name of the course for which the latest IDE settings import has been done.
   */
  @NotNull
  @Override
  public String lastImportedIdeSettings() {
    return PluginSettings.getInstance().getImportedIdeSettingsName();
  }

  /**
   * Downloads the course project settings ZIP file to a temporary file. After that the files from
   * the .idea directory of the ZIP file are extracted to the .idea directory of the given project,
   * after which the project is reloaded. If the course does not provide custom project settings,
   * this method does nothing.
   * @throws IOException                 If an IO error occurs (e.g. network issues).
   * @throws UnexpectedResponseException If an unexpected response is received when downloading the
   *                                     project settings. This usually indicates an error in the
   *                                     course configuration.
   */
  @Override
  public void importProjectSettings(@NotNull Project project, @NotNull Course course)
      throws IOException, UnexpectedResponseException {
    URL settingsUrl = course.getResourceUrls().get("projectSettings");
    if (settingsUrl == null) {
      return;
    }

    Path settingsPath = Paths.get(project.getBasePath(), Project.DIRECTORY_STORE_FOLDER);

    File settingsZip = FileUtilRt.createTempFile(project.getName() + "-settings", ".zip");
    CoursesClient.fetchZip(settingsUrl, settingsZip);
    ZipFile zipFile = new ZipFile(settingsZip);
    List<String> fileNames = zipFile
        .getFileHeaders()
        .stream()
        .filter(file -> !file.isDirectory())
        .map(FileHeader::getFileName)
        .collect(Collectors.toList());

    for (String fileName : fileNames) {
      Path path = Paths.get(fileName);
      Path pathWithoutRoot = path.subpath(1, path.getNameCount());
      zipFile.extractFile(path.toString(), settingsPath.toString(), pathWithoutRoot.toString());
    }

    ProjectManager.getInstance().reloadProject(project);
  }

}
