package fi.aalto.cs.intellij.common;

import java.net.URL;

import org.jetbrains.annotations.NotNull;

public class Module {
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

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public URL getUrl() {
    return url;
  }
}
