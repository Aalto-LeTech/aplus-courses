package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.ComponentLoadException;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.Version;
import fi.aalto.cs.apluscourses.utils.content.RemoteZippedDir;
import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.sbt.project.settings.SbtProjectSettings;

public class SbtModule extends IntelliJModule {
  SbtModule(@NotNull String name,
              @NotNull URL url,
              @NotNull String changelog,
              @NotNull Version version,
              @Nullable Version localVersion,
              @Nullable ZonedDateTime downloadedAt,
              @NotNull APlusProject project,
              @NotNull String originalName) {
    super(name, url, changelog, version, localVersion, downloadedAt, project, originalName);
  }

  SbtModule(@NotNull String name,
            @NotNull URL url,
            @NotNull String changelog,
            @NotNull Version version,
            @Nullable Version localVersion,
            @Nullable ZonedDateTime downloadedAt,
            @NotNull APlusProject project) {
    super(name, url, changelog, version, localVersion, downloadedAt, project);
  }

  @Override
  public void fetchInternal() throws IOException {
    new RemoteZippedDir(getUrl().toString(), getOriginalName()).copyTo(getFullPath(), project.getProject());

    CourseProject courseProject = PluginSettings.getInstance().getCourseProject(project.getProject());
    if (courseProject != null) {
      courseProject.getCourse().getCallbacks().invokePostDownloadModuleCallbacks(project.getProject(), this);
    }
  }

  @Override
  protected void loadInternal() throws ComponentLoadException {
    var settings = new SbtProjectSettings();
    settings.setupNewProjectDefault();
    settings.setExternalProjectPath(ExternalSystemApiUtil.normalizePath(getFullPath().toString()));
    var id = ProjectSystemId.findById("SBT");
    var x = ExternalSystemApiUtil.getManager(id);
    AbstractExternalSystemSettings<?, SbtProjectSettings, ?> extSettings = (AbstractExternalSystemSettings<?, SbtProjectSettings, ?>) x.getSettingsProvider().fun(project.getProject());

    try {
      extSettings.linkProject(settings);
    } catch (AbstractExternalSystemSettings.AlreadyImportedProjectException ex) {
      // this SBT module is already imported; a project-wide refresh is all that is required
    } catch (Exception ex) {
      throw new ComponentLoadException(getName(), ex);
    }

    FileDocumentManager.getInstance().saveAllDocuments();
    ExternalSystemUtil.refreshProjects(new ImportSpecBuilder(project.getProject(), id));

    try {
      PluginSettings.getInstance().getCourseFileManager(project.getProject()).addModuleEntry(this);
    } catch (Exception ex) {
      throw new ComponentLoadException(getName(), ex);
    }
  }

  @Override
  protected @NotNull List<String> computeDependencies() {
    return List.of();
  }

  @Override
  public Module copy(@NotNull String newName) {
    return new SbtModule(newName, url, changelog, version, localVersion, downloadedAt, project, originalName);
  }
}
