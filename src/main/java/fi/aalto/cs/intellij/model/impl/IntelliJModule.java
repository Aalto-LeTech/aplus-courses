package fi.aalto.cs.intellij.model.impl;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.intellij.model.Module;
import fi.aalto.cs.intellij.services.PluginSettings;
import fi.aalto.cs.intellij.utils.DomUtil;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import net.lingala.zip4j.ZipFile;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IntelliJModule extends Module {
  @NotNull
  private final Project project;

  private final String MODULES_XPATH = "/module/component/orderEntry[@type='module']/@module-name";

  /**
   * Constructs a module with the given name and URL.
   *
   * @param name The name of the module.
   * @param url  The URL from which the module can be downloaded.
   */
  public IntelliJModule(@NotNull String name, @NotNull URL url, @NotNull Project project) {
    super(name, url);

    this.project = project;
  }

  @Override
  @NotNull
  protected List<String> getDependencies() throws InstallationFailedException {
    try {
      File file = Paths.get(getPath(), getName() + ".iml").toFile();

     return DomUtil.getNodeListFromXPath(MODULES_XPATH, DomUtil.parse(file)).stream().map(Node::getTextContent).collect(Collectors.toList());

    } catch (Exception e) {
      throw new InstallationFailedException(e);
    }
  }

  @Override
  protected void fetchInternal() throws InstallationFailedException {
    try {
      String zipName = getName() + ".zip";
      File tempFile = FileUtilRt.createTempFile(zipName, null);
      //FileUtils.copyURLToFile(getUrl(), tempFile);
      Files.copy(getTestZipDirPath().resolve(zipName), tempFile.toPath(),
          StandardCopyOption.REPLACE_EXISTING);
      new ZipFile(tempFile).extractAll(getBasePath());
    } catch (Exception e) {
      throw new InstallationFailedException(e);
    }
  }

  @Override
  protected void loadInternal() throws InstallationFailedException {
    try {
      ModuleManager.getInstance(project).loadModule(
          new File(getPath(), getName() + ".iml").getPath());
    } catch (Exception e) {
      throw new InstallationFailedException(e);
    }
  }

  private String getBasePath() {
    return Objects.requireNonNull(project.getBasePath());
  }

  private String getPath() {
    return Paths.get(getBasePath(), getName()).toString();
  }

  private Path getTestZipDirPath() {
    return Paths.get(getBasePath()).getParent().resolve("modules");
  }
}
