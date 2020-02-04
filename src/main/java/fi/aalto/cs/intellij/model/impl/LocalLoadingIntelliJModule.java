package fi.aalto.cs.intellij.model.impl;

import com.intellij.openapi.project.Project;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class LocalLoadingIntelliJModule extends IntelliJModule {
  /**
   * Constructs a module with the given name and URL.
   *
   * @param name    The name of the module.
   * @param url     The URL from which the module can be downloaded.
   * @param project
   */
  public LocalLoadingIntelliJModule(@NotNull String name, @NotNull URL url, @NotNull Project project) {
    super(name, url, project);
  }

  @Override
  protected void fetchZipTo(File file) throws IOException {
    Files.copy(getTestZipDirPath().resolve(getName() + ".zip"), file.toPath(),
        StandardCopyOption.REPLACE_EXISTING);
  }

  private Path getTestZipDirPath() {
    return Paths.get(Objects.requireNonNull(getBasePath())).getParent().resolve("modules");
  }
}
