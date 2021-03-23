package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class CourseVersion {

  public enum Status {
    VALID,
    UPDATE_OPTIONAL,
    UPDATE_REQUIRED
  }

  // the major and minor version of the current plugin build
  private static final int MAJOR_VERSION = 1;
  private static final int MINOR_VERSION = 0;
  public static final CourseVersion DEFAULT_VERSION =
      new CourseVersion(MAJOR_VERSION, MINOR_VERSION);

  private final int major;
  private final int minor;

  /**
   * Constructs a course plugin version directly from arguments.
   */
  public CourseVersion(int major, int minor) {
    this.major = major;
    this.minor = minor;
  }

  /**
   * Constructs a course plugin version from a JSON object.
   */
  public CourseVersion(@NotNull JSONObject jsonObject) {
    this(jsonObject.getInt("major"),
         jsonObject.getInt("minor"));
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
}
