package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Module;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class Callbacks {

  @NotNull
  private final List<Method> postDownloadModuleCallbacks = new ArrayList<>();

  private static void addCallbacksFromArray(@Nullable JSONArray callbackArray,
                                            @NotNull String methodName,
                                            @NotNull List<Method> callbackList) throws ClassNotFoundException {
    if (callbackArray == null) {
      return;
    }

    for (int i = 0; i < callbackArray.length(); i++) {
      String className = callbackArray.getString(i);

      Class<?> callbackClass = Class.forName("fi.aalto.cs.apluscourses.utils.callbacks." + className);
      Method method = Arrays.stream(callbackClass.getDeclaredMethods())
          .filter(m -> m.getName().equals(methodName))
          .findFirst()
          .orElseThrow();

      callbackList.add(method);
    }
  }

  public void invokePostDownloadModuleCallbacks(@NotNull Project project, @NotNull Module module) {
    postDownloadModuleCallbacks.forEach(method -> {
      try {
        method.invoke(null, project, module);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @NotNull
  public static Callbacks fromJsonObject(@NotNull JSONObject callbacksObject) {
    List<Method> postDownloadModuleCallbacks = new ArrayList<>();

    try {
      addCallbacksFromArray(callbacksObject.optJSONArray("postDownloadModule"),
          "postDownloadModule", postDownloadModuleCallbacks);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }

    return new Callbacks(postDownloadModuleCallbacks);
  }

  public Callbacks() {
    this(null);
  }

  public Callbacks(@Nullable List<Method> postDownloadModuleCallbacks) {
    if (postDownloadModuleCallbacks != null) {
      this.postDownloadModuleCallbacks.addAll(postDownloadModuleCallbacks);
    }
  }
}
