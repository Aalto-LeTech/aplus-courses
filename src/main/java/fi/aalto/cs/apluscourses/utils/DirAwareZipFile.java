package fi.aalto.cs.apluscourses.utils;

import java.io.File;
import java.util.Objects;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;

public class DirAwareZipFile extends ZipFile {

  public DirAwareZipFile(@NotNull File zipFile) {
    super(zipFile);
  }

  public void extract(@NotNull String name, @NotNull String destinationPath)
      throws ZipException {
    if (name.equals("/")) {
      extractAll(destinationPath);
    } else if (name.endsWith("/")) {
      extractDir(name, destinationPath);
    } else {
      extractFile(name, destinationPath);
    }
  }

  /**
   * Extracts all the files of the given directory to the given destination path.  The inner
   * directory structure is retained.
   *
   * @param dirName         The name of the directory with a trailing slash.  Must not be empty.
   * @param destinationPath The destination path.
   * @throws ZipException          If there were errors related to ZIP.
   */
  public void extractDir(@NotNull String dirName, @NotNull String destinationPath)
      throws ZipException {
    // File names in ZIP should always use forward slashes.
    // See section 4.4.17 of the ".ZIP File Format Specification" v6.3.6 FINAL.
    // Available online: https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT
    int dirNameLength = dirName.length();

    String[] fileNames = getFileHeaders()
        .stream()
        .map(FileHeader::getFileName)
        .filter(Objects::nonNull)
        .filter(fileName -> fileName.startsWith(dirName))
        .map(fileName -> fileName.substring(dirNameLength))
        .toArray(String[]::new);

    if (fileNames.length == 0) {
      throw new ZipException(dirName, ZipException.Type.FILE_NOT_FOUND);
    }

    for (String fileName : fileNames) {
      if (!fileName.isEmpty()) {
        extractFile(dirName + fileName, destinationPath, fileName);
      }
    }
  }
}
