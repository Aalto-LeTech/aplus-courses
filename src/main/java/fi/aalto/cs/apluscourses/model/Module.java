package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.StateMonitor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public abstract class Module {

  public static final int ERROR = StateMonitor.ERROR;
  public static final int UNLOADED = ERROR - 1;
  public static final int UNINSTALLED = UNLOADED - 2;
  public static final int NOT_INSTALLED = StateMonitor.INITIAL;
  public static final int FETCHING = NOT_INSTALLED + 1;
  public static final int FETCHED = FETCHING + 1;
  public static final int LOADING = FETCHED + 1;
  public static final int LOADED = LOADING + 1;
  public static final int WAITING_FOR_DEPS = LOADED + 1;
  public static final int INSTALLED = WAITING_FOR_DEPS + 1;

  public final Event stateChanged = new Event();
  public final StateMonitor stateMonitor = new StateMonitor(this::onStateChanged);

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
   * @param factory    A {@link ModelFactory} object that is responsible for actual object creation.
   * @return A module constructed from the given JSON object.
   * @throws MalformedURLException  If the URL of the module is malformed.
   * @throws org.json.JSONException If the jsonObject doesn't contain "name" and "url" keys with
   *                                string values.
   */
  @NotNull
  public static Module fromJsonObject(@NotNull JSONObject jsonObject, @NotNull ModelFactory factory)
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

  /**
   * Returns the names of the modules on which this module is dependent.  This method should not be
   * called unless the module is in FETCHED state or further.
   *
   * @return Names of the dependencies, as a {@link List}.
   * @throws ModuleLoadException If dependencies could not be read.
   */
  @NotNull
  public abstract List<String> getDependencies() throws ModuleLoadException;
  
  public abstract void fetch() throws IOException;

  public abstract void load() throws ModuleLoadException;

  /**
   * Checks the state of the module (for an example by looking at the file system) and updates
   * #{@link Module#stateMonitor} to the correct state.
   */
  public abstract void updateState();

  protected void onStateChanged() {
    stateChanged.trigger();
  }

  public boolean hasError() {
    return stateMonitor.get() <= ERROR;
  }
}
