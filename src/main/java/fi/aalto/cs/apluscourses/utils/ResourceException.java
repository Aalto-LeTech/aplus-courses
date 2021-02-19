package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class for exceptions related to a certain resource.
 */
public class ResourceException extends Exception {

  @NotNull
  private final String resourceName;

  /**
   * Constructs a {@link ResourceException} object representing an error that occurred while
   * trying to read a resource.
   * @param message A description of what went wrong.
   * @param cause An {@link Exception} (or other {@link Throwable}) which caused this exception
   *              or, null if there is no such a cause.
   */
  public ResourceException(@NotNull String resourceName,
                           @NotNull String message,
                           @Nullable Throwable cause) {
    super("Resource '" + resourceName + "': " + message, cause);
    
    this.resourceName = resourceName;
  }

  @NotNull
  public String getResourceName() {
    return resourceName;
  }
}
