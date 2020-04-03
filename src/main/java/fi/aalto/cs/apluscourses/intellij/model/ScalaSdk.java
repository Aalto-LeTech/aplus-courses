package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

public class ScalaSdk extends IntelliJLibrary {

  private static final String URL = "https://scala-lang.org/files/archive/";
  private static final String[] ROOTS = {
      "scala-compiler.jar",
      "scala-library.jar",
      "scala-reflect.jar"
  };

  public ScalaSdk(@NotNull String name, @NotNull Project project) {
    super(name, project);
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

  @Override
  protected String[] getUris() {
    Path path = Paths.get(getBasePath(), getFileName(), "lib");
    return Arrays.stream(ROOTS)
        .map(path::resolve)
        .map(Path::toUri)
        .map(URI::toString)
        .toArray(String[]::new);
  }
}
