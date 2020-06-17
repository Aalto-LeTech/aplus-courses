package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;

public class VfsUtil {

  private VfsUtil() {

  }

  /**
   * Tells whether some file inside a directory has changed since a given timestamp.
   *
   * @param dirPath        Path to the directory.
   * @param comparisonTime Epoch milli to compare.
   * @return True or false.
   */
  public static boolean hasDirectoryChanges(Path dirPath, long comparisonTime) {
    VirtualFile virtualFile = com.intellij.openapi.vfs.VfsUtil.findFile(dirPath, true);
    HasChangedVirtualFileVisitor visitor = new HasChangedVirtualFileVisitor(comparisonTime);

    if (virtualFile != null) {
      VfsUtilCore.visitChildrenRecursively(virtualFile, visitor);
    }

    return visitor.hasChanges();
  }

  static class HasChangedVirtualFileVisitor extends VirtualFileVisitor<Object> {

    private volatile boolean hasChanges = false;
    private final long comparisonValue;

    public boolean hasChanges() {
      return hasChanges;
    }

    public HasChangedVirtualFileVisitor(long comparisonValue, @NotNull Option... options) {
      super(options);
      this.comparisonValue = comparisonValue;
    }

    @Override
    public boolean visitFile(@NotNull VirtualFile file) {
      boolean proceedVisiting = !hasChanges && file.getTimeStamp() > comparisonValue;

      if (proceedVisiting) {
        hasChanges = true;
        return false;
      }
      return true;
    }
  }
}
