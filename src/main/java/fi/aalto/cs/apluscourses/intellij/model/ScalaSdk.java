package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.CalledWithReadLock;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.project.ScalaLanguageLevel;
import org.jetbrains.plugins.scala.project.ScalaLibraryProperties;
import org.jetbrains.plugins.scala.project.ScalaLibraryPropertiesState;
import org.jetbrains.plugins.scala.project.ScalaLibraryType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class ScalaSdk extends IntelliJLibrary<PersistentLibraryKind<ScalaLibraryProperties>,
    ScalaLibraryPropertiesState> {

  private static final String URL = "https://scala-lang.org/files/archive/";
  private static final String[] CLASSES = {
      "scala-library.jar",
      "scala-reflect.jar"
  };

  private final String scalaVersion;

  /**
   * Constructs a new Scala SDK object.
   *
   * @param name    Name that must match scala-sdk-0.0.0 pattern.
   * @param project The IntelliJ project.
   */
  public ScalaSdk(@NotNull String name, @NotNull APlusProject project) {
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
    zipFile.extractDir(libDir, getFullPath().toString());
  }

  public String getFileName() {
    return "scala-" + scalaVersion;
  }

  @CalledWithReadLock
  @Override
  protected String[] getUris() {
    return getUris(CLASSES, path -> VfsUtil.getUrlForLibraryRoot(path.toFile()));
  }

  /**
   * Method to filter out SDK library root URLs.
   *
   * @param roots      An array of {@link String} to filter.
   * @param pathToUri  A mapper that creates a URI string from a {@link Path}.
   * @return filtered array of root {@link String}s.
   */
  public String[] getUris(@NotNull String[] roots, Function<Path, String> pathToUri) {
    return Arrays.stream(roots)
        .filter(string -> !string.isEmpty())
        .map(getFullPath()::resolve)
        .map(pathToUri)
        .toArray(String[]::new);
  }

  /**
   * Gets local file system URIs of the given roots.
   *
   * @param roots File names for library class roots.
   * @return An array of URI strings (unescaped).
   */
  @CalledWithReadLock
  public String[] getUris(@NotNull String[] roots) {
    String protocol = LocalFileSystem.getInstance().getProtocol();
    return getUris(roots, path -> VirtualFileManager.constructUrl(protocol,
        FileUtil.toSystemIndependentName(path.toString())));
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
        ScalaLanguageLevel.findByVersion(scalaVersion).get(),
        getUris(getJarFiles())));
  }

}
