package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.ide.startup.StartupActionScriptManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.UpdateSettings;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.DomUtil;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SettingsImporterImpl implements SettingsImporter {

  /**
   * Downloads the course IDE settings ZIP file to a temporary file. Also adds IDEA startup actions
   * that unzip the temporary file to the IDEA configuration path after which the temporary file is
   * deleted. Therefore, the new IDE settings only take effect once IDEA is restarted and the
   * temporary file must still exist at that point.
   * @throws IOException                 If an IO error occurs (e.g. network issues).
   */
  @Override
  public void importIdeSettings(@NotNull Course course) throws IOException {
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
   */
  @Override
  public void importProjectSettings(@NotNull Project project, @NotNull Course course)
      throws IOException {
    URL settingsUrl = course.getResourceUrls().get("projectSettings");
    if (settingsUrl == null) {
      return;
    }

    Path settingsPath = Paths.get(project.getBasePath(), Project.DIRECTORY_STORE_FOLDER);

    File settingsZip = FileUtilRt.createTempFile(project.getName() + "-settings", ".zip");
    CoursesClient.fetchZip(settingsUrl, settingsZip);
    ZipFile zipFile = new ZipFile(settingsZip);

    extractZipTo(zipFile, settingsPath);

    Path workspaceXmlPath = settingsPath.resolve("workspace.xml");
    Document workspaceXml = createCustomWorkspaceXml(workspaceXmlPath);
    DomUtil.writeDocumentToFile(workspaceXml, workspaceXmlPath.toFile());
  }

  private static void extractZipTo(@NotNull ZipFile zipFile, @NotNull Path target)
      throws IOException {
    List<String> fileNames = zipFile
        .getFileHeaders()
        .stream()
        .filter(file -> !file.isDirectory())
        .map(FileHeader::getFileName)
        .collect(Collectors.toList());

    for (String fileName : fileNames) {
      Path path = Paths.get(fileName);
      // The ZIP contains a .idea directory with all of the settings files. We want to extract the
      // files to the .idea directory without the .idea prefix, as otherwise we would end up with
      // .idea/.idea/<settings_files>.
      Path pathWithoutRoot = path.subpath(1, path.getNameCount());
      zipFile.extractFile(path.toString(), target.toString(), pathWithoutRoot.toString());
    }
  }

  private static final String WORKSPACE_XML_COMPONENT_NAME = "CompilerWorkspaceConfiguration";
  private static final String WORKSPACE_XML_OPTION_NAME = "AUTO_SHOW_ERRORS_IN_EDITOR";
  private static final String WORKSPACE_XML_OPTION_VALUE = "false";

  @NotNull
  private static Document createCustomWorkspaceXml(@NotNull Path workspaceXmlPath) throws
      IOException {
    try {
      Document document = DomUtil.parse(workspaceXmlPath.toFile());
      Node projectNode = document.getDocumentElement();

      // Check if a component with the given name already exists
      Node compilerConfigurationNode = DomUtil.findChildNodeWithAttribute(projectNode,
          "component", "name", WORKSPACE_XML_COMPONENT_NAME);

      if (compilerConfigurationNode == null) {
        Element compilerConfigurationElement = document.createElement("component");
        compilerConfigurationElement.setAttribute("name", WORKSPACE_XML_COMPONENT_NAME);
        compilerConfigurationNode = compilerConfigurationElement;
        projectNode.appendChild(compilerConfigurationNode);
      }

      Element option = document.createElement("option");
      option.setAttribute("name", WORKSPACE_XML_OPTION_NAME);
      option.setAttribute("value", WORKSPACE_XML_OPTION_VALUE);
      compilerConfigurationNode.appendChild(option);
      return document;
    } catch (SAXException ex) {
      // If workspace.xml is malformed, then something is seriously wrong...
      throw new IllegalStateException();
    }
  }


}
