package fi.aalto.cs.apluscourses.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public abstract class Module extends Component {

  @NotNull
  private final URL url;

  @NotNull
  private String versionId;

  @NotNull
  private final ZonedDateTime downloadedAt;

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
      @NotNull ZonedDateTime downloadedAt) {
    super(name);
    this.url = url;
    this.versionId = versionId;
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
   *   "id": "abc",
   *   "downloadedAt": "2020-05-29T11:20:42.513813+03:00[Europe/Helsinki]"
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
    String versionId = jsonObject.optString("id");
    if (versionId == null) {
      versionId = "";
    }
    return factory.createModule(name, url, versionId);
  }

  @NotNull
  public URL getUrl() {
    return url;
  }

  /**
   * Returns a string that uniquely identifies the version of this module. That is, a different
   * version of the same module should return a different version string.
   */
  @NotNull
  public String getVersionId() {
    return versionId;
  }

  protected void setVersionId(@NotNull String newVersionId) {
    this.versionId = newVersionId;
  }

  @NotNull
  public ZonedDateTime getDownloadedAt() {
    return downloadedAt;
  }

  public abstract boolean hasLocalChanges();
}
