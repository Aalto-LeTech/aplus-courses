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
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.ui.DuplicateSubmissionDialog;
import fi.aalto.cs.apluscourses.utils.cache.Cache;
import fi.aalto.cs.apluscourses.utils.cache.CachePreferences;
import fi.aalto.cs.apluscourses.utils.cache.JsonFileCache;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    boolean checkForDuplicateSubmission(@NotNull Project project, @NotNull String courseId, long exerciseId,
                                        @NotNull Map<String, Path> files);
  }

  public interface SubmissionGroupSelector {
    boolean isGroupAllowedForExercise(@NotNull Project project, @NotNull String courseId, long exerciseId,
                                      @NotNull Group group);
    
    void onAssignmentSubmitted(@NotNull Project project, @NotNull String courseId, long exerciseId,
                               @NotNull Group group);
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

    /**
     * A convenience class for handling the cached hashes - it facilitates conversion from and to a JSONObject,
     * and provides a simple interface for adding new hashes and checking for already existing hashes.
     */
    private static class SubmissionHashes {
      private static final String JSON_ENTRY_NAME = "hashes";
      private final @NotNull Set<String> hashes;

      public SubmissionHashes(@Nullable JSONObject jsonObject) {
        hashes = new HashSet<>();

        if (jsonObject == null) {
          return;
        }

        final JSONArray hashesJson = jsonObject.optJSONArray(JSON_ENTRY_NAME);
        if (hashesJson == null) {
          return;
        }

        for (int i = 0; i < hashesJson.length(); ++i) {
          try {
            hashes.add(hashesJson.getString(i));
          } catch (JSONException e) {
            hashes.clear();
            return;
          }
        }
      }

      public @NotNull JSONObject toJsonObject() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_ENTRY_NAME, new JSONArray(hashes));

        return jsonObject;
      }

      public boolean containsHash(@NotNull String hash) {
        return hashes.contains(hash);
      }

      public void addHash(@NotNull String hash) {
        hashes.add(hash);
      }
    }

    private static Cache<String, JSONObject> submissionsCache;

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
     * @param project The current IntelliJ project.
     * @param courseId ID of the course which contains the exercise in question.
     * @param exerciseId ID of the exercise.
     * @param files A map of submittable files, as constructed in {@link SubmitExerciseAction}.
     * @return True if duplicate check succeeded and the submission should proceed; false if it should be cancelled.
     */
    public static boolean checkForDuplicateSubmission(@NotNull Project project,
                                                      @NotNull String courseId,
                                                      long exerciseId,
                                                      @NotNull Map<String, Path> files) {
      // we want to defer creation of the cache until it's actually necessary
      if (submissionsCache == null) {
        Path projectPath = Path.of(Objects.requireNonNull(project.getBasePath()));
        submissionsCache = new JsonFileCache(
            projectPath.resolve(Path.of(Project.DIRECTORY_STORE_FOLDER, "a-plus-hashes.json")));
      }

      try {
        final MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
        final StringBuilder submissionString = new StringBuilder();

        // the iteration order is important so that files always get concatenated in the same order
        files.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> {
          submissionString.append(entry.getKey());
          submissionString.append('|'); // a separating character that is not in base-64 alphabet
          submissionString.append(hashFileToString(shaDigest, entry.getValue()));
          submissionString.append(',');
        });

        final String cacheKey = "hash_c" + courseId + "_e" + exerciseId;
        final byte[] finalHashBytes = shaDigest.digest(submissionString.toString().getBytes(StandardCharsets.UTF_8));
        final String finalHash = Base64.getEncoder().encodeToString(finalHashBytes);

        final JSONObject cachedHashesJson = submissionsCache.getValue(cacheKey, CachePreferences.PERMANENT);
        final SubmissionHashes cachedHashes = new SubmissionHashes(cachedHashesJson);

        if (cachedHashes.containsHash(finalHash)) {
          if (!DuplicateSubmissionDialog.showDialog()) {
            return false;
          }
        } else {
          cachedHashes.addHash(finalHash);
        }

        submissionsCache.putValue(cacheKey, cachedHashes.toJsonObject(), CachePreferences.PERMANENT);
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

  public static class SubmissionGroupSelectorImpl implements SubmissionGroupSelector {
    private static final String JSON_ENTRY_NAME = "lastGroup";
    private static Cache<String, JSONObject> groupsCache;

    @NotNull
    private String getCacheKey(@NotNull String courseId, long exerciseId) {
      return "lastGroup_c" + courseId + "_e" + exerciseId;
    }

    private boolean isGroupAllowed(@NotNull JSONObject savedGroupData, @NotNull Group currentGroup) {
      return currentGroup.getMemberwiseId().equals(savedGroupData.optString(JSON_ENTRY_NAME));
    }

    @NotNull
    private JSONObject getGroupCacheObject(@NotNull Group currentGroup) {
      final JSONObject jsonObject = new JSONObject();
      jsonObject.put(JSON_ENTRY_NAME, currentGroup.getMemberwiseId());

      return jsonObject;
    }

    @Override
    public boolean isGroupAllowedForExercise(@NotNull Project project, @NotNull String courseId, long exerciseId,
                                             @NotNull Group group) {
      if (groupsCache == null) {
        return true;
      }

      final String cacheKey = getCacheKey(courseId, exerciseId);
      final JSONObject cachedGroupJson = groupsCache.getValue(cacheKey, CachePreferences.PERMANENT);

      if (cachedGroupJson == null) {
        return true;
      }

      return isGroupAllowed(cachedGroupJson, group);
    }

    @Override
    public void onAssignmentSubmitted(@NotNull Project project, @NotNull String courseId, long exerciseId,
                                      @NotNull Group group) {
      if (groupsCache == null) {
        Path projectPath = Path.of(Objects.requireNonNull(project.getBasePath()));
        groupsCache = new JsonFileCache(
            projectPath.resolve(Path.of(Project.DIRECTORY_STORE_FOLDER, "a-plus-groups.json")));
      }

      final String cacheKey = getCacheKey(courseId, exerciseId);
      groupsCache.putValue(cacheKey, getGroupCacheObject(group), CachePreferences.PERMANENT);
    }
  }
}
