package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
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
  private static final String[] CLASSES = {
      "scala-library.jar",
      "scala-reflect.jar"
  };

  // Sonar, unjustifiably, hates reference-typed volatile fields
  private volatile String[] allClasses = null; //NOSONAR

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

  /**
   * Method to extract contents of the .zip file.
   *
   * @param file a zip {@link File} to extract.
   * @throws IOException is thrown if zip can't be extracted.
   */
  public void extractZip(File file) throws IOException {
    DirAwareZipFile zipFile = new DirAwareZipFile(file);
    String libDir = getFileName() + "/lib";
    System.err.println("libDir " + libDir);
    System.err.println("getFullPath().toString() " + getFullPath().toString());
    zipFile.extractDir(libDir, getFullPath().toString());
  }

  public Path getFullPath() {
    return project.getBasePath().resolve(getPath());
  }

  public String getFileName() {
    return "scala-" + scalaVersion;
  }

  @Override
  protected String[] getUris() {
    return getUris(CLASSES);
  }

  /**
   * Method to filter out SDK library root URIs.
   *
   * @param roots an array of {@link String} to filter.
   * @return filtered array of root {@link String}s.
   */
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

  private String[] getJarFiles() {
    File[] files = Objects.requireNonNull(getFullPath().toFile().listFiles());
    return Stream.of(files)
        .map(File::getName)
        .filter(fileName -> fileName.endsWith(".jar"))
        .toArray(String[]::new);
  }

  @Override
  @CalledWithWriteLock
  public void initializeLibraryProperties(
      LibraryProperties<ScalaLibraryPropertiesState> properties) {
    properties.loadState(new ScalaLibraryPropertiesState(
        ScalaLanguageLevel.findByVersion(scalaVersion).get(), getUris(getJarFiles())));
  }
}