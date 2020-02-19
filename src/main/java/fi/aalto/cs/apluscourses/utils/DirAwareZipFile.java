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
   * Extracts all the files of the given directory to the given destination path.  The directory
   * structure is retained, including the directory given as an argument.
   *
   * @param dirName         The name of the directory without a trailing slash.  Must not be empty.
   * @param destinationPath The destination path.  Note that all the extracted files are located in
   *                        subdirectory {@code dirName} of the destinationPath.
   * @throws ZipException          If there were errors related to ZIP.
   * @throws FileNotFoundException If the given directory does not exist in the ZIP.
   */
  public void extractDir(@NotNull String dirName, @NotNull String destinationPath)
      throws ZipException, FileNotFoundException {
    // File names in ZIP should always use forward slashes.
    // See section 4.4.17 of the ".ZIP File Format Specification" v6.3.6 FINAL.
    // Available online: https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT
    String prefix = dirName + "/";

    if (getFileHeader(prefix) == null) {
      throw new FileNotFoundException(dirName);
    }

    String[] fileNames = getFileHeaders()
        .stream()
        .map(FileHeader::getFileName)
        .filter(Objects::nonNull)
        .filter(fileName -> fileName.startsWith(prefix))
        .toArray(String[]::new);

    for (String fileName : fileNames) {
      extractFile(fileName, destinationPath);
    }
  }
}
