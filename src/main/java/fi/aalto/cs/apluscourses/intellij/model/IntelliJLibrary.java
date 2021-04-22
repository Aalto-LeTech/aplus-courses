package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.concurrency.annotations.RequiresWriteLock;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.utils.content.Content;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IntelliJLibrary
    <K extends PersistentLibraryKind<? extends LibraryProperties<S>>, S>
    extends Library
    implements IntelliJComponent<com.intellij.openapi.roots.libraries.Library> {

  @NotNull
  protected final APlusProject project;

  protected IntelliJLibrary(@NotNull String name, @NotNull APlusProject project) {
    super(name);
    this.project = project;
  }

  @Override
  public void fetch() throws IOException {
    for (Content content : getContents()) {
      content.copyTo(getFullPath());
    }
  }

  @NotNull
  protected abstract Content @NotNull[] getContents();

  @RequiresWriteLock
  @SuppressWarnings("unchecked")
  protected void loadInternal() {
    LibraryTable.ModifiableModel libraryTable = project.getLibraryTable().getModifiableModel();
    var library = libraryTable
        .createLibrary(getName(), getLibraryKind())
        .getModifiableModel();
    for (String uri : getClassUris()) {
      library.addRoot(uri, OrderRootType.CLASSES);
    }

    //HACK: this is the only way to access properties that I am aware of
    var libraryEx = (LibraryEx.ModifiableModelEx) library;
    LibraryProperties<S> properties = libraryEx.getProperties();
    if (properties != null) {
      var newState = getPropertiesState(properties.getState());
      if (newState != null) {
        properties.loadState(newState);
        libraryEx.setProperties(properties);
      }
    }

    library.commit();
    libraryTable.commit();
  }

  @Override
  public void load() {
    WriteAction.runAndWait(this::loadInternal);
  }

  @Override
  public void unload() {
    super.unload();
    WriteAction.runAndWait(this::unloadInternal);
  }

  @RequiresWriteLock
  private void unloadInternal() {
    LibraryTable libraryTable = project.getLibraryTable();
    com.intellij.openapi.roots.libraries.Library library = getPlatformObject();
    if (library != null) {
      libraryTable.removeLibrary(library);
    }
  }

  @Override
  public void remove() throws IOException {
    FileUtils.deleteDirectory(getFullPath().toFile());
  }

  @NotNull
  @Override
  public Path getPath() {
    return Paths.get("lib", getName());
  }

  @Override
  @NotNull
  public Path getFullPath() {
    return project.getBasePath().resolve(getPath());
  }

  @Override
  protected int resolveStateInternal() {
    return project.resolveComponentState(this);
  }

  /**
   * URIs MUST NOT be escaped!  Note that java.net.URI does escaping so avoid using that.
   *
   * @return URIs to be included in classes of the library.
   */
  @RequiresReadLock
  protected String[] getClassUris() {
    return getUris(getClassRoots(), path -> VfsUtil.getUrlForLibraryRoot(path.toFile()));
  }

  protected String[] getUris(@NotNull String[] roots, Function<Path, String> pathToUri) {
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
  @RequiresReadLock
  protected String[] getUris(@NotNull String[] roots) {
    String protocol = LocalFileSystem.getInstance().getProtocol();
    return getUris(roots, path -> VirtualFileManager.constructUrl(protocol,
        FileUtil.toSystemIndependentName(path.toString())));
  }

  @NotNull
  protected String[] getClassRoots() {
    return getJarFiles(); // use all JAR files by default
  }

  @Nullable
  protected K getLibraryKind() {
    return null; // default to null ("normal")
  }

  /**
   * Helper method that returns all the JAR files in the library path.
   * @return An array of filenames.
   */
  @NotNull
  protected String[] getJarFiles() {
    File[] files = Objects.requireNonNull(getFullPath().toFile().listFiles());
    return Stream.of(files)
        .map(File::getName)
        .filter(fileName -> fileName.endsWith(".jar"))
        .toArray(String[]::new);
  }

  @Nullable
  protected S getPropertiesState(@Nullable S currentState) {
    return currentState;
  }


  @Override
  @RequiresReadLock
  @Nullable
  public com.intellij.openapi.roots.libraries.Library getPlatformObject() {
    return project.getLibraryTable().getLibraryByName(getName());
  }
}
