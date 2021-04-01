package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Version;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.Nullable;

public class ModuleMetadata {

  @Nullable
  private final Version version;
  @Nullable
  private final ZonedDateTime downloadedAt;

  public ModuleMetadata(@Nullable Version version,
                        @Nullable ZonedDateTime downloadedAt) {
    this.version = version;
    this.downloadedAt = downloadedAt;
  }

  @Nullable
  public Version getVersion() {
    return version;
  }

  @Nullable
  public ZonedDateTime getDownloadedAt() {
    return downloadedAt;
  }
}
