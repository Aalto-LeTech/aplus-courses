package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.CalledWithReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VfsUtil {

  private VfsUtil() {

  }

  /**
   * Recursively looks for a file in the given directory with the given name.
   *
   * @param directory The directory from which the search is done.
   * @param filename  The name of the file that is searched for.
   * @return A {@link Path} to the found file, or {@code null} if a file isn't found.
   */
  @CalledWithReadLock
  @Nullable
  public static Path findFileInDirectory(@NotNull Path directory, @NotNull String filename) {
    VirtualFile virtualFile = com.intellij.openapi.vfs.VfsUtil.findFile(directory, true);
    FileFinderVirtualFileVisitor visitor = new FileFinderVirtualFileVisitor(filename);
    if (virtualFile == null) {
      return null;
    }
    com.intellij.openapi.vfs.VfsUtilCore.visitChildrenRecursively(virtualFile, visitor);
    return visitor.getPath();
  }

  static class FileFinderVirtualFileVisitor extends VirtualFileVisitor<Object> {
    private String filename;
    private Path path;

    public FileFinderVirtualFileVisitor(@NotNull String filename, Option... options) {
      super(options);
      this.filename = filename;
      this.path = null;
    }

    @Nullable
    public Path getPath() {
      return path;
    }

    @Override
    public boolean visitFile(@NotNull VirtualFile file) {
      if (path != null) {
        return false;
      }

      if (file.getName().equals(filename)) {
        path = Paths.get(file.getPath());
        return false;
      }

      return true;
    }
  }

  /**
   * Tells whether some file inside a directory has changed since a given timestamp.
   *
   * @param dirPath        Path to the directory.
   * @param comparisonTime Epoch milli to compare.
   * @return True or false.
   */
  @CalledWithReadLock
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
