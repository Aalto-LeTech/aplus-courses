package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class CoursePluginVersion {

  public enum Status {
    VALID,
    UPDATE_OPTIONAL,
    UPDATE_REQUIRED
  }

  // the major and minor version of the current plugin build
  private static final int MAJOR_VERSION = 1;
  private static final int MINOR_VERSION = 1;
  public static final CoursePluginVersion CURRENT_VERSION =
          new CoursePluginVersion(MAJOR_VERSION, MINOR_VERSION, "");

  private final int major;
  private final int minor;

  @Nullable
  private final String prettyVersion;

  private CoursePluginVersion(int major, int minor, @Nullable String prettyVersion) {
    this.major = major;
    this.minor = minor;
    this.prettyVersion = prettyVersion;
  }

  /**
   * Constructs a course plugin version from a JSON object.
   */
  public CoursePluginVersion(@NotNull JSONObject jsonObject) {
    this(jsonObject.getInt("major"),
         jsonObject.getInt("minor"),
         jsonObject.optString("prettyVersion"));
  }

  /**
   * Determines whether the current plugin version is recent enough for the given plugin version.
   */
  public Status checkVersion() {
    if (MAJOR_VERSION < major) {
      return Status.UPDATE_REQUIRED;
    }
    if (MAJOR_VERSION > major) {
      return Status.VALID;
    }
    return MINOR_VERSION >= minor ? Status.VALID : Status.UPDATE_OPTIONAL;
  }

  public @Nullable String getPrettyVersion() {
    return prettyVersion;
  }
}
