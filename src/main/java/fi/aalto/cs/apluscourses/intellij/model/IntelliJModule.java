package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.model.ComponentLoadException;
import fi.aalto.cs.apluscourses.model.Module;
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

  IntelliJModule(@NotNull String name, @NotNull URL url, @NotNull Project project) {
    super(name, url);
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
      throw new ComponentLoadException(this, e);
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
      throw new ComponentLoadException(this, e);
    }
  }

  @Override
  public void updateState() {
    /*
     * Three cases to check for here:
     *   1. The module is in the project, so its state should be INSTALLED.
     *   2. The module is not in the project but the module files are present in the file system, so
     *      its state should be FETCHED.
     *   3. The module files aren't present in the file system (and by extension the module isn't in
     *      the project), so its state should be NOT_INSTALLED.
     */
    if (ModuleManager.getInstance(project).findModuleByName(getName()) != null) {
      stateMonitor.set(Module.INSTALLED);
    } else if (new File(getFullPath()).isDirectory()) {
      // We assume, that if the project directory contains a directory with the path of a module,
      // then the module files are present. Of course, it's possible that some relevant module files
      // have been removed from the directory.
      stateMonitor.set(Module.FETCHED);
    } else {
      stateMonitor.set(Module.NOT_INSTALLED);
    }
  }

  @NotNull
  private File createTempFile() throws IOException {
    return FileUtilRt.createTempFile(getName(), ".zip");
  }

  private void extractZip(File file) throws IOException {
    // ZIP may contain other dirs (typically, dependency modules) but we only extract the files that
    // belongs to this module.
    new DirAwareZipFile(file).extractDir(getName(), getFullParentPath());
  }


  private void fetchZipTo(File file) throws IOException {
    try {
      CoursesClient.fetchZip(getUrl(), file);
    } catch (UnexpectedResponseException ex) {
      // At this point, the URL is most likely incorrect or the server is missing the file.
      throw new IOException(ex);
    }
  }

  @NotNull
  private String getBasePath() {
    return Objects.requireNonNull(getProject().getBasePath());
  }

  @NotNull
  private String getFullPath() {
    return Paths.get(getBasePath(), getRelativePath()).toString();
  }

  @NotNull
  private String getFullParentPath() {
    return Paths.get(getFullPath()).getParent().toString();
  }

  @NotNull
  private File getImlFile() {
    return Paths.get(getFullPath(), getName() + ".iml").toFile();
  }

  @NotNull
  public String getRelativePath() {
    return getName();
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  private static class Loader {
    private final ModuleManager moduleManager;
    private final String imlFileName;

    public Loader(Project project, @NotNull File imlFile) {
      moduleManager = ModuleManager.getInstance(project);
      imlFileName = imlFile.toString();
    }

    @CalledWithWriteLock
    public void load() throws JDOMException, ModuleWithNameAlreadyExists, IOException {
      moduleManager.loadModule(imlFileName);
    }
  }
}
