package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Interfaces {
  @FunctionalInterface
  public interface AuthenticationProvider {
    Authentication getAuthentication(@Nullable Project project);
  }

  @FunctionalInterface
  public interface Tagger {
    void putSystemLabel(@Nullable Project project, @NotNull String tag, int color);
  }

  @FunctionalInterface
  public interface DocumentSaver {
    void saveAllDocuments();
  }

  @FunctionalInterface
  public interface LanguageSource {
    @NotNull String getLanguage(@NotNull Project project);
  }

  @FunctionalInterface
  public interface AssistantModeProvider {
    boolean isAssistantMode();
  }

  @FunctionalInterface
  public interface FileRefresher {
    void refreshPath(VirtualFile file, Runnable callback);
  }

  @FunctionalInterface
  public interface FileBrowser {
    void navigateTo(File file, Project project);
  }

  @FunctionalInterface
  public interface VirtualFileFinder {
    VirtualFile findFile(File file);
  }

  public interface CollapsedPanels {
    void setCollapsed(@NotNull String title);

    void setExpanded(@NotNull String title);
  }

  public static class FileRefresherImpl {
    private FileRefresherImpl() {

    }

    /**
     * Refreshes the VFS.
     */
    public static void refreshPath(VirtualFile file, Runnable callback) {
      var refresher = RefreshQueue.getInstance().createSession(true, true, callback);
      if (file != null) {
        refresher.addFile(file);
      }
      refresher.launch();
    }
  }

  public static class FileBrowserImpl {
    private FileBrowserImpl() {

    }

    /**
     * Opens the file in the editor.
     */
    public static void navigateTo(File file, Project project) {
      var vf = LocalFileSystem.getInstance().findFileByIoFile(file);
      if (vf != null) {
        new OpenFileDescriptor(project, vf).navigate(true);
      }
    }
  }

  public static class VirtualFileFinderImpl {
    private VirtualFileFinderImpl() {

    }

    /**
     * Finds a file.
     */
    @Nullable
    public static VirtualFile findVirtualFile(File file) {
      return LocalFileSystem.getInstance().findFileByIoFile(file);
    }
  }

  public static class CollapsedPanelsImpl implements Interfaces.CollapsedPanels {

    @Override
    public void setCollapsed(@NotNull String title) {
      PluginSettings.getInstance().setCollapsed(title);
    }

    @Override
    public void setExpanded(@NotNull String title) {
      PluginSettings.getInstance().setExpanded(title);
    }
  }
}
