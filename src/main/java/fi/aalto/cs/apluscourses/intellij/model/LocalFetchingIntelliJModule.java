package fi.aalto.cs.apluscourses.intellij.model;

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

// Remove this class when modules are no longer fetched from a local dir.
class LocalFetchingIntelliJModule extends IntelliJModule {

  LocalFetchingIntelliJModule(@NotNull String name,
                              @NotNull URL url,
                              @NotNull Project project) {
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
