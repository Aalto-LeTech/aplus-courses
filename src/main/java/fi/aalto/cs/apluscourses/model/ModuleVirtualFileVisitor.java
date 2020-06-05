package fi.aalto.cs.apluscourses.model;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;

public class ModuleVirtualFileVisitor extends VirtualFileVisitor<Object> {

  private volatile boolean hasChanges = false;
  private final long downloadedAt;

  public boolean hasChanges() {
    return hasChanges;
  }

  public ModuleVirtualFileVisitor(ZonedDateTime downloadedAt, @NotNull Option... options) {
    super(options);
    this.downloadedAt = downloadedAt.toInstant().toEpochMilli();
  }

  @Override
  public boolean visitFile(@NotNull VirtualFile file) {
    boolean proceedVisiting = !hasChanges && file.getTimeStamp()
        > downloadedAt + PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION;

    if (proceedVisiting) {
      hasChanges = true;
      return false;
    }
    return true;
  }
}
