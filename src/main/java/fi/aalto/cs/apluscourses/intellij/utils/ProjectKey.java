package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that makes it easy to use projects as keys in maps. The equals and hashCode methods are
 * based on the project base path. No reference to the project is stored, only a string containing
 * the path.
 */
public class ProjectKey {
  @NotNull
  private final String projectPath;

  /**
   * Construct an instance with the given project.
   */
  public ProjectKey(@Nullable Project project) {
    if (project == null || project.isDefault()) {
      this.projectPath = "";
    } else {
      this.projectPath = project.getBasePath();
    }
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ProjectKey)) {
      return false;
    }
    return projectPath.equals(((ProjectKey) other).projectPath);
  }

  @Override
  public int hashCode() {
    return projectPath.hashCode();
  }
}
