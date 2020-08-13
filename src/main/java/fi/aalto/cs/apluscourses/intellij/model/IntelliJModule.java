package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.CourseFileManager;
import fi.aalto.cs.apluscourses.intellij.utils.ListDependenciesPolicy;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.model.ComponentLoadException;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.CalledWithReadLock;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IntelliJModule
    extends Module
    implements IntelliJComponent<com.intellij.openapi.module.Module> {

  private static final Logger logger = LoggerFactory.getLogger(IntelliJModule.class);

  @NotNull
  private final APlusProject project;

  IntelliJModule(@NotNull String name,
                 @NotNull URL url,
                 @NotNull String versionId,
                 @Nullable String localVersionId,
                 @Nullable ZonedDateTime downloadedAt,
                 @NotNull APlusProject project) {
    super(name, url, versionId, localVersionId, downloadedAt);
    this.project = project;
  }

  @NotNull
  @Override
  public Path getPath() {
    return Paths.get(name);
  }

  @NotNull
  @Override
  public Path getFullPath() {
    return project.getBasePath().resolve(getPath());
  }

  @Override
  public void fetchInternal() throws IOException {
    File tempZipFile = createTempFile();
    fetchZipTo(tempZipFile);
    extractZip(tempZipFile);
  }

  @Override
  protected int resolveStateInternal() {
    return project.resolveComponentState(this);
  }

  @Override
  public void load() throws ComponentLoadException {
    WriteAction.runAndWait(this::loadInternal);
  }

  @CalledWithWriteLock
  private void loadInternal() throws ComponentLoadException {
    try {
      project.getModuleManager().loadModule(getImlFile().toString());
      CourseFileManager.getInstance().addEntryForModule(this);
    } catch (Exception e) {
      throw new ComponentLoadException(getName(), e);
    }
  }

  @Override
  public void unload() {
    super.unload();
    WriteAction.runAndWait(this::unloadInternal);
  }

  @CalledWithWriteLock
  private void unloadInternal() {
    com.intellij.openapi.module.Module module = getPlatformObject();
    if (module != null) {
      project.getModuleManager().disposeModule(module);
    }
  }

  @Override
  public void remove() throws IOException {
    FileUtils.deleteDirectory(getFullPath().toFile());
  }

  @NotNull
  @Override
  protected List<String> computeDependencies() {
    ModuleRootManager moduleRootManager = project.getModuleRootManager(getName());
    if (moduleRootManager == null) {
      throw new IllegalStateException();
    }
    return Objects.requireNonNull(moduleRootManager
        .orderEntries()
        .withoutSdk()
        .withoutModuleSourceEntries()
        .productionOnly()
        .process(new ListDependenciesPolicy(), new ArrayList<>()));
  }

  @NotNull
  private File createTempFile() throws IOException {
    return FileUtilRt.createTempFile(getName(), ".zip");
  }

  private void extractZip(File file) throws IOException {
    // ZIP may contain other dirs (typically, dependency modules) but we only extract the files that
    // belongs to this module.
    new DirAwareZipFile(file).extractDir(getName(), getFullPath().toString());
  }

  /*
   * This method looks for a special ID file in the module files root. If the file is found, the
   * contents of the file are returned. If the file doesn't exist, or any IO error occurs, null is
   * returned. Differentiating between a missing file and IO errors isn't important here, as we can
   * always fall back to the ID from the course configuration file, and the ID file is optional.
   * This method should only be called after extractZip has been called.
   */
  @Override
  @Nullable
  protected String readVersionId() {
    File idFile = getFullPath().resolve(".module_id").toFile();
    try {
      return FileUtils.readFileToString(idFile, StandardCharsets.UTF_8);
    } catch (IOException ignored) {
      return null;
    }
  }

  private void fetchZipTo(File file) throws IOException {
    CoursesClient.fetch(getUrl(), file);
  }

  @NotNull
  private File getImlFile() {
    return getFullPath().resolve(getName() + ".iml").toFile();
  }

  @NotNull
  public APlusProject getProject() {
    return project;
  }

  @Override
  @CalledWithReadLock
  @Nullable
  public com.intellij.openapi.module.Module getPlatformObject() {
    return project.getModuleManager().findModuleByName(getName());
  }

  @Override
  protected boolean hasLocalChanges(@NotNull ZonedDateTime downloadedAt) {
    Path fullPath = getFullPath();
    long timeStamp = downloadedAt.toInstant().toEpochMilli()
        + PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION;
    return ReadAction.compute(() -> VfsUtil.hasDirectoryChanges(fullPath, timeStamp));
  }

}
