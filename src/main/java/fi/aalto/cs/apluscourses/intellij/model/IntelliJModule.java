package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.model.ComponentLoadException;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import fi.aalto.cs.apluscourses.utils.DomUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.jdom.JDOMException;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

class IntelliJModule extends Module {

  @NotNull
  private final APlusProject project;

  // XPath to the names of dependency modules in an *.iml file.
  // <module> is the root node, <component> is its child.  Dependency modules are those <orderEntry>
  // children of <component> for which type="module".  The name of the dependency modules are
  // module-name attributes of these <orderEntry> nodes.
  //
  // Sonar suppression because Sonar thinks this is a filepath URI, which should not be hardcoded.
  private static final String DEPENDENCY_NAMES_XPATH =
      "/module/component/orderEntry[@type='module']/@module-name"; // NOSONAR
  private static final String LIBRARY_NAMES_XPATH =
      "/module/component/orderEntry[@type='library']/@name"; //NOSONAR

  IntelliJModule(@NotNull String name, @NotNull URL url, @NotNull APlusProject project, int state) {
    super(name, url, state);
    this.project = project;
  }

  @NotNull
  private List<String> getStringsFromXPath(String xpath) throws ComponentLoadException {
    try {
      return DomUtil.getNodesFromXPath(xpath, getImlFile())
          .stream()
          .map(Node::getTextContent)
          .collect(Collectors.toList());
    } catch (IOException | SAXException e) {
      throw new ComponentLoadException(getName(), e);
    }
  }

  @Override
  @NotNull
  public List<String> getDependencyModules() throws ComponentLoadException {
    return getStringsFromXPath(DEPENDENCY_NAMES_XPATH);
  }

  @Override
  @NotNull
  public List<String> getLibraries() throws ComponentLoadException {
    return getStringsFromXPath(LIBRARY_NAMES_XPATH);
  }

  @NotNull
  @Override
  public Path getPath() {
    return project.getModulePath(getName());
  }

  @Override
  public void fetch() throws IOException {
    File tempZipFile = createTempFile();
    fetchZipTo(tempZipFile);
    extractZip(tempZipFile);
  }

  @Override
  public void load() throws ComponentLoadException {
    try {
      WriteAction.runAndWait(new Loader(getProject(), getImlFile())::load);
    } catch (Exception e) {
      throw new ComponentLoadException(getName(), e);
    }
  }

  @NotNull
  private File createTempFile() throws IOException {
    return FileUtilRt.createTempFile(getName(), ".zip");
  }

  private void extractZip(File file) throws IOException {
    String fullPath = project.getBasePath().resolve(getPath()).toString();

    // ZIP may contain other dirs (typically, dependency modules) but we only extract the files that
    // belongs to this module.
    new DirAwareZipFile(file).extractDir(getName(), fullPath);
  }


  private void fetchZipTo(File file) throws IOException {
    CoursesClient.fetchZip(getUrl(), file);
  }

  @NotNull
  private File getImlFile() {
    return project.getBasePath().resolve(getPath()).resolve(getName() + ".iml").toFile();
  }

  @NotNull
  public APlusProject getProject() {
    return project;
  }

  private static class Loader {
    private final ModuleManager moduleManager;
    private final String imlFileName;

    public Loader(APlusProject project, @NotNull File imlFile) {
      moduleManager = project.getModuleManager();
      imlFileName = imlFile.toString();
    }

    @CalledWithWriteLock
    public void load() throws JDOMException, ModuleWithNameAlreadyExists, IOException {
      moduleManager.loadModule(imlFileName);
    }
  }
}
