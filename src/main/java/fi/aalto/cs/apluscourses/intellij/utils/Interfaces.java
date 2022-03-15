package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import fi.aalto.cs.apluscourses.intellij.actions.SubmitExerciseAction;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.ui.DuplicateSubmissionDialog;
import fi.aalto.cs.apluscourses.utils.cache.Cache;
import fi.aalto.cs.apluscourses.utils.cache.CachePreferences;
import fi.aalto.cs.apluscourses.utils.cache.StringFileCache;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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

    private static Cache<String, String> submissionsCache;

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

    /**
     * Checks whether the same submission has already been submitted by the user. If it is the case, the user
     * is asked whether they want to submit anyway or abort the submission.
     * @param courseId ID of the course which contains the exercise in question.
     * @param exerciseId ID of the exercise.
     * @param files A map of submittable files, as constructed in {@link SubmitExerciseAction}.
     * @return True if duplicate check succeeded and the submission should proceed; false if it should be cancelled.
     */
    public static boolean checkForDuplicateSubmission(@NotNull String courseId,
                                                      long exerciseId,
                                                      @NotNull Map<String, Path> files) {

      if (submissionsCache == null) {
        submissionsCache = new StringFileCache(Path.of(Project.DIRECTORY_STORE_FOLDER, "a-plus-hashes.json"));
      }

      try {
        final MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
        final StringBuilder submissionString = new StringBuilder();

        files.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> {
          submissionString.append(entry.getKey());
          submissionString.append('|'); // a separating character that is not in base-64 alphabet
          submissionString.append(hashFileToString(shaDigest, entry.getValue()));
          submissionString.append(',');
        });

        String cacheKey = "hash_c" + courseId + "_e" + exerciseId;
        byte[] finalHashBytes = shaDigest.digest(submissionString.toString().getBytes(StandardCharsets.UTF_8));
        String finalHash = Base64.getEncoder().encodeToString(finalHashBytes);

        String cachedHashes = submissionsCache.getValue(cacheKey, CachePreferences.PERMANENT);
        if (cachedHashes == null) {
          submissionsCache.putValue(cacheKey, finalHash, CachePreferences.PERMANENT);
          return true;
        }

        List<String> existingHashes = Arrays.asList(cachedHashes.split(","));
        if (existingHashes.contains(finalHash)) {
          if (!DuplicateSubmissionDialog.showDialog()) {
            return false;
          }
        } else {
          existingHashes.add(finalHash);
        }

        submissionsCache.putValue(cacheKey, String.join(",", existingHashes), CachePreferences.PERMANENT);
        return true;
      } catch (UncheckedIOException | NoSuchAlgorithmException e) {
        // if an exception occurs, don't bother the user and just proceed with submitting as normal
        // it's highly probable that in such case, submitting will fail later on anyway, and the error will be logged
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
