package fi.aalto.cs.apluscourses.utils;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
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

  /**
   * Extracts all the files of the given directory to the given destination path.  The inner
   * directory structure is retained.
   *
   * @param dirName         The name of the directory without a trailing slash.  Must not be empty.
   * @param destinationPath The destination path.
   * @throws ZipException          If there were errors related to ZIP.
   */
  public void extractDir(@NotNull String dirName,
                         @NotNull String destinationPath,
                         @NotNull Project project)
      throws ZipException {
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
      throw new ZipException(dirName, ZipException.Type.FILE_NOT_FOUND);
    }
    var progressViewModel =
        PluginSettings.getInstance().getMainViewModel(project).progressViewModel;
    var progress = progressViewModel.start(fileNames.length,
        getText("ui.ProgressBarView.unpacking"), false);

    for (String fileName : fileNames) {
      if (!fileName.isEmpty()) {
        progress.setLabel(getAndReplaceText("ui.ProgressBarView.unpackingFile", fileName));
        extractFile(prefix + fileName, destinationPath, fileName);
      }
      progress.increment();
    }
    progress.finish();
  }
}
