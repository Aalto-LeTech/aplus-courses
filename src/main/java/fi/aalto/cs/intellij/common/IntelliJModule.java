package fi.aalto.cs.intellij.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.sun.jna.platform.win32.NTSecApi;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import net.lingala.zip4j.ZipFile;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class IntelliJModule extends Module {
  @NotNull
  private final Project project;

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
  protected List<String> getDependencies() throws Exception {
    File file = Paths.get(getPath(), getName() + ".iml").toFile();
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);

    NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath()
        .compile("/module/component/orderEntry[@type='module']/@module-name")
        .evaluate(document, XPathConstants.NODESET);

    List<String> result = new ArrayList<>();
    for (int i = 0, length = nodeList.getLength(); i < length; i++) {
      result.add(nodeList.item(i).getTextContent());
    }
    return result;
  }

  @Override
  protected void fetchInternal() throws Exception {
    String zipName = getName() + ".zip";
    File tempFile = FileUtilRt.createTempFile(zipName, null);
    //FileUtils.copyURLToFile(getUrl(), tempFile);
    Files.copy(getTestZipDirPath().resolve(zipName), tempFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);
    new ZipFile(tempFile).extractAll(getBasePath());
  }

  @Override
  protected void loadInternal() throws Exception {
    ModuleManager.getInstance(project).loadModule(
        new File(getPath(), getName() + ".iml").getPath());
  }

  private String getBasePath() {
    return Objects.requireNonNull(project.getBasePath());
  }

  @Override
  public String getPath() {
    return Paths.get(getBasePath(), getName()).toString();
  }

  private Path getTestZipDirPath() {
    return Paths.get(getBasePath()).getParent().resolve("modules");
  }
}
