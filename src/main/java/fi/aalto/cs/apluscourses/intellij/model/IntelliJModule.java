package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleLoadException;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import fi.aalto.cs.apluscourses.utils.DomUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jdom.JDOMException;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

class IntelliJModule extends Module {

  @NotNull
  private final Project project;

  // XPath to the names of dependency modules in an *.ilm file.
  // <module> is the root node, <component> is its child.  Dependency modules are those <orderEntry>
  // children of <component> for which type="module".  The name of the dependency modules are
  // module-name attributes of these <orderEntry> nodes.
  //
  // Sonar suppression because Sonar thinks this is a filepath URI, which should not be hardcoded.
  private static final String DEPENDENCY_NAMES_XPATH =
      "/module/component/orderEntry[@type='module']/@module-name"; // NOSONAR

  IntelliJModule(@NotNull String name, @NotNull URL url, @NotNull Project project) {
    super(name, url);

    this.project = project;
  }

  @Override
  @NotNull
  public List<String> getDependencies() throws ModuleLoadException {
    try {
      return DomUtil.getNodesFromXPath(DEPENDENCY_NAMES_XPATH, getImlFile())
          .stream()
          .map(Node::getTextContent)
          .collect(Collectors.toList());
    } catch (IOException | SAXException e) {
      throw new ModuleLoadException(this, e);
    }
  }

  @Override
  public void fetch() throws IOException {
    File tempZipFile = createTempFile();
    fetchZipTo(tempZipFile);
    extractZip(tempZipFile);
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

    // ZIP may contain other dirs (typically, dependency modules) but we only extract the files that
    // belongs to this module.
    new DirAwareZipFile(file).extractDir(getName(), getBasePath());
  }


  protected void fetchZipTo(File file) throws IOException {
    try {
      CoursesClient.fetchZip(getUrl(), file);
    } catch (UnexpectedResponseException ex) {
      // At this point, the URL is most likely incorrect or the server is missing the file.
      throw new IOException(ex);
    }
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
