package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.ListDependenciesPolicy;
import fi.aalto.cs.apluscourses.model.ComponentLoadException;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jdom.JDOMException;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class IntelliJModule
    extends Module
    implements IntelliJComponent<com.intellij.openapi.module.Module> {

  @NotNull
  private final APlusProject project;

  IntelliJModule(@NotNull String name,
      @NotNull URL url,
      @NotNull String versionId,
      @NotNull LocalDateTime downloadedAt,
      @NotNull APlusProject project) {
    super(name, url, versionId, downloadedAt);
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
  public void fetch() throws IOException {
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
    try {
      WriteAction.runAndWait(new Loader(getProject(), getImlFile())::load);
      project.addCourseFileEntry(this);
    } catch (Exception e) {
      throw new ComponentLoadException(getName(), e);
    }
  }

  @Override
  public void unload() {
    super.unload();
    Optional.ofNullable(getPlatformObject()).ifPresent(project.getModuleManager()::disposeModule);
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


  private void fetchZipTo(File file) throws IOException {
    CoursesClient.fetchZip(getUrl(), file);
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
  @Nullable
  public com.intellij.openapi.module.Module getPlatformObject() {
    return project.getModuleManager().findModuleByName(getName());
  }

  @Override
  public boolean hasLocalChanges() {
    VirtualFile virtualFile = VfsUtil.findFile(getFullPath(), true);
    final boolean[] hasChanges = {false};
    long downloadedAt = getDownloadedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

    if (virtualFile != null) {
      VfsUtilCore.visitChildrenRecursively(virtualFile, new VirtualFileVisitor<Object>() {
        @Override
        public boolean visitFile(@NotNull VirtualFile file) {
          boolean proceedVisiting = !hasChanges[0] && file.getTimeStamp()
              > downloadedAt + PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION;

          if (proceedVisiting) {
            hasChanges[0] = true;
            return false;
          }
          return true;
        }
      });
    }
    return hasChanges[0];
  }

  @Override
  public void update() {
    // todo: @nikke234 implement me!
  }

  private static class Loader {

    private final ModuleManager moduleManager;
    private final String imlFileName;

    public Loader(APlusProject project, @NotNull File imlFile) {
      moduleManager = project.getModuleManager();
      imlFileName = imlFile.toString();
    }

    @CalledWithWriteLock
    public void load()
        throws JDOMException, ModuleWithNameAlreadyExists, IOException {
      moduleManager.loadModule(imlFileName);
    }
  }
}
