package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.util.io.FileUtilRt;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
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
  private static final String[] ALL_CLASSES = {
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
  public ScalaSdk(@NotNull String name, @NotNull APlusProject project, int state) {
    super(name, project, state);

    scalaVersion = name.replace("scala-sdk-", "");
  }

  @Override
  public void fetch() throws IOException {
    File tempZipFile = createTempFile();
    fetchZipTo(tempZipFile);
    extractZip(tempZipFile);
  }

  @NotNull
  public File createTempFile() throws IOException {
    return FileUtilRt.createTempFile(getFileName(), ".zip");
  }

  public void fetchZipTo(File file) throws IOException {
    FileUtils.copyURLToFile(new URL(URL + getFileName() + ".zip"), file);
  }

  public void extractZip(File file) throws IOException {
    String prefix = getFileName() + "/lib/";
    String destinationPath = project.getBasePath().resolve(getPath()).toString();
    ZipFile zipFile = new ZipFile(file);
    for (String jarFile : ALL_CLASSES) {
      zipFile.extractFile(prefix + jarFile, destinationPath, jarFile);
    }
  }

  public String getFileName() {
    return "scala-" + scalaVersion;
  }

  @Override
  protected String[] getUris() {
    return getUris(CLASSES);
  }

  public String[] getUris(@NotNull String[] roots) {
    return Arrays.stream(roots)
        .filter(string -> !string.isEmpty())
        .map(project.getBasePath().resolve(getPath())::resolve)
        .map(Path::toUri)
        .map(URI::toString)
        .toArray(String[]::new);
  }

  @Override
  public PersistentLibraryKind<ScalaLibraryProperties> getLibraryKind() {
    return ScalaLibraryType.Kind$.MODULE$;
  }

  @Override
  @CalledWithWriteLock
  public void initializeLibraryProperties(
      LibraryProperties<ScalaLibraryPropertiesState> properties) {
    properties.loadState(new ScalaLibraryPropertiesState(
        ScalaLanguageLevel.findByVersion(scalaVersion).get(), getUris(ALL_CLASSES)));
  }
}

