package fi.aalto.cs.intellij.model.impl;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.intellij.model.Module;
import fi.aalto.cs.intellij.utils.DomUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

public class IntelliJModule extends Module {
  @NotNull
  private final Project project;

  private static final String DEPENDENCY_NAMES =
      "/module/component/orderEntry[@type='module']/@module-name";

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
  public List<String> getDependencies() throws IOException {
    return DomUtil.getNodesFromXPath(DEPENDENCY_NAMES, getImlFile())
        .stream()
        .map(Node::getTextContent)
        .collect(Collectors.toList());
  }

  @Override
  public void fetch() throws IOException {
    File file = createTempFile();
    fetchZipTo(file);
    extractZip(file);
  }

  @Override
  public void load() throws IOException {
    try {
      ModuleManager.getInstance(getProject()).loadModule(getImlFile().toString());
    } catch (JDOMException e) {
      throw new IOException(e);
    } catch (ModuleWithNameAlreadyExists ignored) {
      // We think about this later.  Until then, we just consider this case fine.
    }
  }

  @NotNull
  protected File createTempFile() throws IOException {
    return FileUtilRt.createTempFile(getName(), ".zip");
  }

  protected void extractZip(File file) throws IOException {
    new ZipFile(file).extractAll(getBasePath());
  }

  protected void fetchZipTo(File file) throws IOException {
    FileUtils.copyURLToFile(getUrl(), file);
  }

  protected String getBasePath() {
    return Objects.requireNonNull(getProject().getBasePath());
  }

  protected File getImlFile() {
    String name = getName();
    return Paths.get(getBasePath(), name, name + ".iml").toFile();
  }

  @NotNull
  public Project getProject() {
    return project;
  }
}
