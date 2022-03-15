package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.utils.cache.Cache;
import fi.aalto.cs.apluscourses.utils.cache.CacheImpl;
import fi.aalto.cs.apluscourses.utils.cache.StringFileCache;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Interfaces {
  @FunctionalInterface
  public interface ModuleDirGuesser {
    @Nullable
    VirtualFile guessModuleDir(@NotNull Module module);
  }

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
  public interface DuplicateSubmissionChecker {
    boolean checkForDuplicateSubmission(@NotNull String courseId, long exerciseId, @NotNull Map<String, Path> files);
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

  public interface ReadNews {
    void setNewsRead(long id);

    String getReadNews();
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

  public static class DuplicateSubmissionCheckerImpl {

    private final Cache<String, String> submissionsCache = new StringFileCache(
        Path.of(Project.DIRECTORY_STORE_FOLDER, "a-plus-hashes.json"));

    private DuplicateSubmissionCheckerImpl() {

    }

    private static String hashFileToString(@NotNull MessageDigest digest, @NotNull Path path) {
      try {
        byte[] hash = digest.digest(Files.readAllBytes(path));
        return Base64.getEncoder().encodeToString(hash);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    public static boolean checkForDuplicateSubmission(@NotNull String courseId,
                                                      long exerciseId,
                                                      @NotNull Map<String, Path> files) {

      try {
        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
        StringBuilder submissionString = new StringBuilder();

        files.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> {
          submissionString.append(entry.getKey());
          submissionString.append('|'); // a separating character that is not in base-64 alphabet
          submissionString.append(hashFileToString(shaDigest, entry.getValue()));
          submissionString.append(',');
        });

        byte[] finalHashBytes = shaDigest.digest(submissionString.toString().getBytes(StandardCharsets.UTF_8));
        String finalHash = Base64.getEncoder().encodeToString(finalHashBytes);
        //submissionsCache.getValue()

        return true;
      } catch (UncheckedIOException | NoSuchAlgorithmException e) {
        return true;
      }
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

  public static class ReadNewsImpl implements ReadNews {
    @Override
    public void setNewsRead(long id) {
      PluginSettings.getInstance().setNewsRead(id);
    }

    @Override
    public String getReadNews() {
      return PluginSettings.getInstance().getReadNews();
    }
  }
}
