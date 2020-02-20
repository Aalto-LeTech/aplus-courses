package fi.aalto.cs.apluscourses.utils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.junit.Test;

public class DirAwareZipFileTest {

  // Because it is impossible(?) to make Zip4J to work in a mocked file system (they don't do unit
  // tests for unzipping even themselves, just integration tests), we mock the testable object
  // itself but call those actual methods that we are going to test.  Not best solution because
  // tests become dependent on the implementation, but there are not many options.

  @Test(expected = FileNotFoundException.class)
  public void testExtractDirFileNotFound() throws ZipException, FileNotFoundException {
    DirAwareZipFile zipFile = mock(DirAwareZipFile.class);
    doCallRealMethod().when(zipFile).extractDir(anyString(), anyString());

    doReturn(null).when(zipFile).getFileHeader(anyString());

    zipFile.extractDir("non-existent-dir", "whatever-path");
  }

  @Test
  public void testExtractDir() throws ZipException, FileNotFoundException {
    DirAwareZipFile zipFile = mock(DirAwareZipFile.class);
    doCallRealMethod().when(zipFile).extractDir(anyString(), anyString());

    String dir1 = "dir1/";
    FileHeader dirHeader1 = mock(FileHeader.class);
    doReturn(dir1).when(dirHeader1).getFileName();
    doReturn(dirHeader1).when(zipFile).getFileHeader(dir1);

    String file1 = "dir1/file";
    FileHeader fileHeader1 = mock(FileHeader.class);
    doReturn(file1).when(fileHeader1).getFileName();

    String dir2 = "dir2/";
    FileHeader dirHeader2 = mock(FileHeader.class);
    doReturn(dir2).when(dirHeader2).getFileName();

    String file2 = "file";
    FileHeader fileHeader2 = mock(FileHeader.class);
    doReturn(file2).when(fileHeader2).getFileName();

    List<FileHeader> fileHeaders = new ArrayList<>();
    fileHeaders.add(dirHeader1);
    fileHeaders.add(fileHeader1);
    fileHeaders.add(dirHeader2);
    fileHeaders.add(fileHeader2);
    doReturn(fileHeaders).when(zipFile).getFileHeaders();

    String destinationPath = "destination";

    String dir = "dir1";

    zipFile.extractDir(dir, destinationPath);

    // Verifying the obvious so that we can check no-more-interactions at the end
    verify(zipFile).extractDir(dir, destinationPath);
    verify(zipFile).getFileHeader(dir1);
    verify(zipFile).getFileHeaders();

    verify(zipFile).extractFile(dir1, destinationPath);
    verify(zipFile).extractFile(file1, destinationPath);

    verifyNoMoreInteractions(zipFile);
  }
}
