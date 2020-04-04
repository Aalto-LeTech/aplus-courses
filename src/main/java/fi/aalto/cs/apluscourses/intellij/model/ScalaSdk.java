package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.utils.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.project.ScalaLanguageLevel;
import org.jetbrains.plugins.scala.project.ScalaLibraryProperties;
import org.jetbrains.plugins.scala.project.ScalaLibraryPropertiesState;
import org.jetbrains.plugins.scala.project.ScalaLibraryType;

public class ScalaSdk extends IntelliJLibrary<PersistentLibraryKind<ScalaLibraryProperties>,
    ScalaLibraryPropertiesState> {

  private static final String URL = "https://scala-lang.org/files/archive/";
  private static final String[] COMPILER_CLASSES = {
      "scala-compiler.jar",
      "scala-library.jar",
      "scala-reflect.jar"
  };
  private static final String[] CLASSES = {
      "scala-library.jar",
      "scala-reflect.jar"
  };

  private final String scalaVersion;

  /**
   * Constructs a new Scala SDK object.
   *
   * @param name Name that must match scala-sdk-0.0.0 pattern.
   * @param project The IntelliJ project.
   */
  public ScalaSdk(@NotNull String name, @NotNull Project project) {
    super(name, project);

    scalaVersion = name.replace("scala-sdk-", "");
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
    String prefix = getFileName() + "/lib/";
    String destinationPath = getPath();
    String[] jarFiles = CommonUtil.unionArrays(CLASSES, COMPILER_CLASSES, String[]::new);
    ZipFile zipFile = new ZipFile(file);
    for (String jarFile : jarFiles) {
      zipFile.extractFile(prefix + jarFile, destinationPath, jarFile);
    }
  }

  private String getFileName() {
    return "scala-" + scalaVersion;
  }

  @Override
  protected String[] getUris() {
    return getUris(CLASSES);
  }

  private String[] getUris(String[] roots) {
    Path path = Paths.get(getPath());
    return Arrays.stream(roots)
        .map(path::resolve)
        .map(Path::toUri)
        .map(URI::toString)
        .toArray(String[]::new);
  }

  @Override
  protected PersistentLibraryKind<ScalaLibraryProperties> getLibraryKind() {
    return ScalaLibraryType.Kind$.MODULE$;
  }

  @Override
  @CalledWithWriteLock
  protected void initializeLibraryProperties(
      LibraryProperties<ScalaLibraryPropertiesState> properties) {
    properties.loadState(new ScalaLibraryPropertiesState(
        ScalaLanguageLevel.findByVersion(scalaVersion).get(), getUris(COMPILER_CLASSES)));
  }
}

