package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleLoadException;
import fi.aalto.cs.apluscourses.utils.DomUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.lingala.zip4j.ZipFile;
import org.jdom.JDOMException;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

class IntelliJModule extends Module {

  @NotNull
  private final Project project;

  private static final String DEPENDENCY_NAMES =
      "/module/component/orderEntry[@type='module']/@module-name";

  IntelliJModule(@NotNull String name, @NotNull URL url, @NotNull Project project) {
    super(name, url);

    this.project = project;
  }

  @Override
  @NotNull
  public List<String> getDependencies() throws ModuleLoadException {
    try {
      return DomUtil.getNodesFromXPath(DEPENDENCY_NAMES, getImlFile())
          .stream()
          .map(Node::getTextContent)
          .collect(Collectors.toList());
    } catch (IOException | SAXException e) {
      throw new ModuleLoadException(this, e);
    }
  }

  @Override
  public void fetch() throws IOException {
    File file = createTempFile();
    fetchZipTo(file);
    extractZip(file);
  }

  @Override
  public void load() throws ModuleLoadException {
    try {
      WriteAction.runAndWait(new Loader(getProject(), getImlFile())::load);
    } catch (Exception e) {
      throw new ModuleLoadException(this, e);
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
    // Call HTTP client
    throw new UnsupportedOperationException();
  }

  @NotNull
  protected String getBasePath() {
    return Objects.requireNonNull(getProject().getBasePath());
  }

  @NotNull
  protected File getImlFile() {
    String name = getName();
    return Paths.get(getBasePath(), name, name + ".iml").toFile();
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  private static class Loader {
    private final ModuleManager moduleManager;
    private final String imlFileName;

    public Loader(Project project, File imlFile) {
      moduleManager = ModuleManager.getInstance(project);
      imlFileName = imlFile.toString();
    }

    @CalledWithWriteLock
    public void load() throws JDOMException, ModuleWithNameAlreadyExists, IOException {
      moduleManager.loadModule(imlFileName);
    }
  }
}
