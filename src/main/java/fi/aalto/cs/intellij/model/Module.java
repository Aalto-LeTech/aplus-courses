package fi.aalto.cs.intellij.model;

import com.intellij.openapi.application.WriteAction;
import fi.aalto.cs.intellij.utils.ObservableProperty;
import fi.aalto.cs.intellij.utils.StateMonitor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class Module {

  public static final int ERROR = StateMonitor.ERROR;
  public static final int NOT_INSTALLED = StateMonitor.INITIAL;
  public static final int FETCHING = NOT_INSTALLED + 1;
  public static final int FETCHED = FETCHING + 1;
  public static final int LOADING = FETCHED + 1;
  public static final int LOADED = LOADING + 1;
  public static final int INSTALLED = LOADED + 1;

  private final StateMonitor stateMonitor = new StateMonitor(this::onStateChanged);

  public final ObservableProperty<Integer> state = new ObservableProperty<>(NOT_INSTALLED);

  @NotNull
  private final String name;
  @NotNull
  private final URL url;

  /**
   * Constructs a module with the given name and URL.
   * @param name The name of the module.
   * @param url The URL from which the module can be downloaded.
   */
  public Module(@NotNull String name, @NotNull URL url) {
    this.name = name;
    this.url = url;
  }

  /**
   * Returns a module constructed from the given JSON object. The object should contain the name of
   * the module and the URL from which the module can be downloaded. Additional members in the
   * object don't cause any errors. Example of a valid JSON object:
   * <pre>
   * {
   *   "name": "My Module",
   *   "url": "https://example.com"
   * }
   * </pre>
   * @param jsonObject The JSON object containing information about a single module.
   * @return A module constructed from the given JSON object.
   * @throws MalformedURLException  If the URL of the module is malformed.
   * @throws org.json.JSONException If the jsonObject doesn't contain "name" and "url" keys with
   *                                string values.
   */
  @NotNull
  public static Module fromJsonObject(@NotNull JSONObject jsonObject, CourseFactory factory)
      throws MalformedURLException {
    String name = jsonObject.getString("name");
    URL url = new URL(jsonObject.getString("url"));
    return factory.createModule(name, url);
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public URL getUrl() {
    return url;
  }

  public CompletableFuture<Void> installAsync(ModuleSource moduleSource) {
    return CompletableFuture.runAsync(() -> installInternal(moduleSource));
  }

  private void installInternal(ModuleSource moduleSource) {
    try {
      fetch();
      load(moduleSource);
    } catch (Exception e) {
      stateMonitor.set(ERROR);
    }
  }

  private void fetch()
      throws InstallationFailedException, InterruptedException {
    if (stateMonitor.setConditionally(NOT_INSTALLED, FETCHING)) {
      fetchInternal();
      stateMonitor.set(FETCHED);
    } else {
      stateMonitor.waitFor(FETCHED);
    }
  }

  private void load(ModuleSource moduleSource)
      throws InstallationFailedException, InterruptedException {
    if (stateMonitor.setConditionally(FETCHED, LOADING)) {
      CompletableFuture<Void> installDependencies = installDependenciesAsync(moduleSource);
      new Loader().load();
      stateMonitor.set(LOADED);
      installDependencies.join();
      stateMonitor.set(INSTALLED);
    } else {
      stateMonitor.waitFor(LOADED);
    }
  }

  private CompletableFuture<Void> installDependenciesAsync(ModuleSource moduleSource)
    throws InstallationFailedException{
    return CompletableFuture.allOf(getDependencies()
        .stream()
        .map(moduleSource::getModule)
        .filter(Objects::nonNull)
        .map(module -> module.installAsync(moduleSource))
        .toArray(CompletableFuture[]::new)
    );
  }

  @NotNull
  protected List<String> getDependencies() throws InstallationFailedException {
    return new ArrayList<>();
  }

  protected void fetchInternal() throws InstallationFailedException {
    throw new UnsupportedOperationException();
  }

  protected void loadInternal() throws InstallationFailedException {
    throw new UnsupportedOperationException();
  }

  private void onStateChanged() {
    state.set(stateMonitor.get());
  }

  protected static class InstallationFailedException extends Exception {
    public InstallationFailedException(@Nullable Throwable throwable) {
      super(throwable);
    }
  }

  public interface ModuleSource {
    @Nullable
    Module getModule(String moduleName);
  }

  private class Loader {
    // Sonar does not like non-primitive-type volatile fields because apparently people easily
    // misunderstand their semantics, but we know what we are doing here.
    private volatile Exception exception = null; //NOSONAR

    private void load() throws InstallationFailedException {
      WriteAction.runAndWait(this::loadInEventThread);
      Exception e = exception;
      if (e != null) {
        throw new InstallationFailedException(e);
      }
    }

    private void loadInEventThread() {
      try {
        loadInternal();
      } catch (Exception e) {
        exception = e;
      }
    }
  }
}
