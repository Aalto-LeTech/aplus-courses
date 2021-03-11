package fi.aalto.cs.apluscourses.model;

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

  private final int major;
  private final int minor;

  @Nullable
  private final String prettyVersion;

  public CoursePluginVersion(JSONObject jsonObject) {
    this(jsonObject.getInt("major"),
         jsonObject.getInt("minor"),
         jsonObject.optString("prettyVersion"));
  }

  public CoursePluginVersion(int major, int minor, @Nullable String prettyVersion) {
    this.major = major;
    this.minor = minor;
    this.prettyVersion = prettyVersion;
  }

  public Status checkVersion() {
    if (MAJOR_VERSION < major) {
      return Status.UPDATE_REQUIRED;
    }
    if (MAJOR_VERSION > major) {
      return Status.VALID;
    }
    return MINOR_VERSION >= minor ? Status.VALID : Status.UPDATE_OPTIONAL;
  }
}
