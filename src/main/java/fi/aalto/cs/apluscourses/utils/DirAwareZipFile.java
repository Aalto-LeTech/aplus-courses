package fi.aalto.cs.apluscourses.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;

public class DirAwareZipFile extends ZipFile {


  public DirAwareZipFile(@NotNull File zipFile) {
    super(zipFile);
  }

  /**
   * Extracts all the files of the given directory to the given destination path.  The inner
   * directory structure is retained.
   *
   * @param dirName         The name of the directory without a trailing slash.  Must not be empty.
   * @param destinationPath The destination path.
   * @throws ZipException          If there were errors related to ZIP.
   * @throws FileNotFoundException If the given directory does not exist in the ZIP.
   */
  public void extractDir(@NotNull String dirName, @NotNull String destinationPath)
      throws ZipException, FileNotFoundException {
    // File names in ZIP should always use forward slashes.
    // See section 4.4.17 of the ".ZIP File Format Specification" v6.3.6 FINAL.
    // Available online: https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT
    String prefix = dirName + "/";
    int prefixLength = prefix.length();

    String[] fileNames = getFileHeaders()
        .stream()
        .map(FileHeader::getFileName)
        .filter(Objects::nonNull)
        .filter(fileName -> fileName.startsWith(prefix))
        .map(fileName -> fileName.substring(prefixLength))
        .toArray(String[]::new);

    if (fileNames.length == 0) {
      throw new FileNotFoundException(dirName);
    }

    for (String fileName : fileNames) {
      if (!fileName.isEmpty()) {
        extractFile(prefix + fileName, destinationPath, fileName);
      }
    }
  }
}
