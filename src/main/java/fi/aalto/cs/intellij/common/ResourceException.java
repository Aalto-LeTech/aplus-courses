package fi.aalto.cs.intellij.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class for exceptions related to a certain resource.
 */
public class ResourceException extends Exception {

  @NotNull
  private final Resources resources;
  @NotNull
  private final String resourceName;

  /**
   * Constructs a {@link ResourceException} object representing an error that occurred while
   * trying to read a resource.
   * @param message A description of what went wrong.
   * @param cause An {@link Exception} (or other {@link Throwable}) which caused this exception
   *              or, null if there is no such a cause.
   */
  public ResourceException(@NotNull Resources resources,
                           @NotNull String resourceName,
                           @NotNull String message,
                           @Nullable Throwable cause) {
    super("Resource '" + resourceName + "': " + message, cause);
    this.resources = resources;
    this.resourceName = resourceName;
  }

  @NotNull
  public Resources getResources() {
    return resources;
  }

  @NotNull
  public String getResourceName() {
    return resourceName;
  }
}
