package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.model.ComponentLoadException;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

public class ScalaSdk extends Library {

  private static final String URL = "https://scala-lang.org/files/archive/";
  private static final String ROOTS = "scala-compiler.jar scala-library.jar scala-reflect.jar";
  @NotNull
  private final Project project;

  public ScalaSdk(@NotNull String name, @NotNull Project project) {
    super(name);
    this.project = project;
  }

  @Override
  public void fetch() throws IOException {
    File tempZipFile = createTempFile();
    fetchZipTo(tempZipFile);
    extractZip(tempZipFile);
  }

  @NotNull
  private File createTempFile() throws IOException {
    return FileUtilRt.createTempFile(getFileName(), ".zip");
  }

  private void fetchZipTo(File file) throws IOException {
    FileUtils.copyURLToFile(new URL(URL + getFileName() + ".zip"), file);
  }

  private void extractZip(File file) throws IOException {
    new DirAwareZipFile(file).extractDir(getFileName() + "/lib", getBasePath());
  }

  private String getFileName() {
    return getName().replace("scala-sdk", "scala");
  }

  @NotNull
  private String getBasePath() {
    return Objects.requireNonNull(getProject().getBasePath());
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  @Override
  public void load() throws ComponentLoadException {
    try {
      WriteAction.runAndWait(this::loadInternal);
    } catch (MalformedURLException e) {
      throw new ComponentLoadException(this, e);
    }
  }

  private void loadInternal() throws MalformedURLException {
    com.intellij.openapi.roots.libraries.Library.ModifiableModel modifiableModel =
        LibraryTablesRegistrar.getInstance()
            .getLibraryTable(project)
            .createLibrary(getName())
            .getModifiableModel();
    for (String root : ROOTS.split(" ")) {
      String uri = Paths.get(getBasePath(), getFileName(), "lib", root).toUri().toString();
      modifiableModel.addRoot(uri, OrderRootType.CLASSES);
    }
    modifiableModel.commit();
  }
}
