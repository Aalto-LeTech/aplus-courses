package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.callbacks.AddModuleWatermark;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class Callbacks {

  @FunctionalInterface
  public interface PostDownloadModuleCallback {
    void postDownloadModule(@NotNull Project project, @NotNull Module module);
  }

  @NotNull
  private static final Map<String, PostDownloadModuleCallback> availablePostDownloadModuleCallbacks = Map.of(
      "AddModuleWatermark", AddModuleWatermark::postDownloadModule
  );

  @NotNull
  private final List<PostDownloadModuleCallback> postDownloadModuleCallbacks = new ArrayList<>();

  private static <T> void addCallbacksFromArray(@Nullable JSONArray callbackArray,
                                                @NotNull Map<String, T> sourceCallbacks,
                                                @NotNull List<T> targetCallbackList) {
    if (callbackArray == null) {
      return;
    }

    for (int i = 0; i < callbackArray.length(); i++) {
      String callbackName = callbackArray.getString(i);
      T callback = sourceCallbacks.get(callbackName);
      if (callback != null) {
        targetCallbackList.add(callback);
      }
    }
  }

  public void invokePostDownloadModuleCallbacks(@NotNull Project project, @NotNull Module module) {
    postDownloadModuleCallbacks.forEach(callback -> callback.postDownloadModule(project, module));
  }

  /**
   * Constructs an instance of callbacks from the given JSON object.
   */
  @NotNull
  public static Callbacks fromJsonObject(@NotNull JSONObject callbacksObject) {
    List<PostDownloadModuleCallback> postDownloadModuleCallbacks = new ArrayList<>();
    addCallbacksFromArray(callbacksObject.optJSONArray("postDownloadModule"),
        availablePostDownloadModuleCallbacks, postDownloadModuleCallbacks);

    return new Callbacks(postDownloadModuleCallbacks);
  }

  private Callbacks(@Nullable List<PostDownloadModuleCallback> postDownloadModuleCallbacks) {
    if (postDownloadModuleCallbacks != null) {
      this.postDownloadModuleCallbacks.addAll(postDownloadModuleCallbacks);
    }
  }

  /**
   * Constructor without any callbacks.
   */
  public Callbacks() {
    this(null);
  }
}
