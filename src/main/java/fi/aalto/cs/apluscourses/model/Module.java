package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public abstract class Module extends Component {

  @NotNull
  private final URL url;

  /* A string that uniquely identifies the version of this module. That is, a different version of
     the same module should return a different version string. */
  @NotNull
  private String versionId;
  @Nullable
  private String localVersionId;
  @Nullable
  private ZonedDateTime downloadedAt;

  /* synchronize with this when accessing variable fields of this class */
  private final Object versionLock = new Object();

  /**
   * Constructs a module with the given name and URL.
   *
   * @param name      The name of the module.
   * @param url       The URL from which the module can be downloaded.
   * @param versionId A string that uniquely identifies different versions of the same module.
   */
  public Module(@NotNull String name,
                @NotNull URL url,
                @NotNull String versionId,
                @Nullable String localVersionId,
                @Nullable ZonedDateTime downloadedAt) {
    super(name);
    this.url = url;
    this.versionId = versionId;
    this.localVersionId = localVersionId;
    this.downloadedAt = downloadedAt;
  }

  /**
   * Returns a module constructed from the given JSON object. The object should contain the name of
   * the module and the URL from which the module can be downloaded. The object may optionally also
   * contain a version id. Additional members in the object don't cause any errors. Example of a
   * valid JSON object:
   * <pre>
   * {
   *   "name": "My Module",
   *   "url": "https://example.com",
   *   "id": "abc"
   * }
   * </pre>
   *
   * @param jsonObject The JSON object containing information about a single module.
   * @param factory    A {@link ModelFactory} object that is responsible for actual object
   *                   creation.
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
    String versionId = jsonObject.optString("id", "");
    return factory.createModule(name, url, versionId);
  }

  @Override
  public void fetch() throws IOException {
    fetchInternal();
    String newId = readVersionId();
    synchronized (versionLock) {
      downloadedAt = ZonedDateTime.now();
      if (newId != null) {
        versionId = newId;
      }
      localVersionId = versionId;
    }
  }

  /**
   * Tells whether or not the module is updatable.
   *
   * @return True, if the module is loaded and the local version is not the newest one; otherwise
   *        false.
   */
  @Override
  public boolean isUpdatable() {
    if (stateMonitor.get() != LOADED) {
      return false;
    }
    synchronized (versionLock) {
      return !versionId.equals(localVersionId);
    }
  }

  protected abstract void fetchInternal() throws IOException;

  @Nullable
  protected abstract String readVersionId();

  @NotNull
  public URL getUrl() {
    return url;
  }

  /**
   * Tells whether or not the module has local changes.
   *
   * @return True if there are local changes, otherwise false.
   */
  @Override
  public boolean hasLocalChanges() {
    ZonedDateTime downloadedAtVal;
    synchronized (versionLock) {
      downloadedAtVal = this.downloadedAt;
    }
    if (downloadedAtVal == null) {
      return false;
    }
    return hasLocalChanges(downloadedAtVal);
  }

  protected abstract boolean hasLocalChanges(@NotNull ZonedDateTime downloadedAt);

  /**
   * Returns metadata (data that should be stored locally) of the module.
   *
   * @return A {@link ModuleMetadata} object.
   */
  @NotNull
  public ModuleMetadata getMetadata() {
    synchronized (versionLock) {
      return new ModuleMetadata(Optional.ofNullable(localVersionId).orElse(versionId),
          downloadedAt);
    }
  }

  /**
   * Returns the version ID (not local).
   *
   * @return Version ID.
   */
  public String getVersionId() {
    synchronized (versionLock) {
      return versionId;
    }
  }
}
