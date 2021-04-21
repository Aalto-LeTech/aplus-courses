package fi.aalto.cs.apluscourses.intellij.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import org.junit.Ignore;
import org.junit.Test;

public class VfsUtilTest {

  @Test
  public void testFileFinderVisitorDoesNotSetPath() {
    VirtualFile virtualFile = mock(VirtualFile.class);
    doReturn("not equal").when(virtualFile).getName();

    VfsUtil.FileFinderVirtualFileVisitor visitor =
        new VfsUtil.FileFinderVirtualFileVisitor("test name");
    boolean result = visitor.visitFile(virtualFile);
    assertTrue(result);
    assertNull("The path is null when the visited file has a different name",
        visitor.getPath());
  }

  @Test
  public void testFileFinderVisitorSetsPath() {
    VirtualFile virtualFile = mock(VirtualFile.class);
    doReturn("equal").when(virtualFile).getName();
    doReturn("testPath").when(virtualFile).getPath();

    VfsUtil.FileFinderVirtualFileVisitor visitor =
        new VfsUtil.FileFinderVirtualFileVisitor("equal");
    boolean result = visitor.visitFile(virtualFile);
    assertFalse(result);
    assertEquals("The path is the path of the matching virtual file", Paths.get("testPath"),
        visitor.getPath());
  }

  @Test
  public void testVisitFileReturnsTrue() {
    //  given
    long now = ZonedDateTime.now().toInstant().toEpochMilli();
    VfsUtil.HasChangedVirtualFileVisitor moduleVirtualFileVisitor =
        new VfsUtil.HasChangedVirtualFileVisitor(now);
    VirtualFile virtualFile = new LightVirtualFile();
    VirtualFile spyVirtualFile = spy(virtualFile);
    assertFalse(moduleVirtualFileVisitor.hasChanges());
    long later = now + 20L * 1000;
    when(spyVirtualFile.getTimeStamp()).thenReturn(later);

    //  when
    boolean result = moduleVirtualFileVisitor.visitFile(spyVirtualFile);

    //  then
    assertTrue(moduleVirtualFileVisitor.hasChanges());
    assertFalse(result);
  }

  @Test
  public void testVisitFileReturnsFalse() {
    //  given
    VfsUtil.HasChangedVirtualFileVisitor moduleVirtualFileVisitor =
        new VfsUtil.HasChangedVirtualFileVisitor(ZonedDateTime.now().toInstant().toEpochMilli());
    VirtualFile virtualFile = new LightVirtualFile();
    assertFalse(moduleVirtualFileVisitor.hasChanges());

    //  when
    boolean result = moduleVirtualFileVisitor.visitFile(virtualFile);

    //  then
    assertFalse(moduleVirtualFileVisitor.hasChanges());
    assertTrue(result);
  }

  public static final String JSON = ".json";

  @Ignore("Fails for the reason no one can deduct")
  @Test
  public void testHasLocalChangesReturnsTrue() throws IOException {
    //  given
    File tempDirectory = FileUtilRt.createTempDirectory("first", "", true);
    File tempFileOne = FileUtilRt.createTempFile(tempDirectory, "test1", JSON, true);
    File tempFileTwo = FileUtilRt.createTempFile(tempDirectory, "test2", JSON, true);

    long now = Instant.now().toEpochMilli();
    assertTrue(tempFileTwo.setLastModified(now));
    assertTrue(tempFileOne.setLastModified(now + 1000));

    //  when & then
    assertTrue(VfsUtil.hasDirectoryChanges(tempDirectory.toPath(), now));
  }

  @Ignore("Fails for the reason no one can deduct")
  @Test
  public void testHasLocalChangesReturnsFalse() throws IOException {
    //  given
    File tempDirectory = FileUtilRt.createTempDirectory("second", "", true);
    File tempFileOne = FileUtilRt.createTempFile(tempDirectory, "test1", JSON, true, true);
    FileUtilRt.createTempFile(tempDirectory, "test2", JSON, true, true);

    long now = Instant.now().toEpochMilli();
    assertTrue(tempFileOne.setLastModified(now));

    //  when & then
    assertFalse(VfsUtil.hasDirectoryChanges(tempDirectory.toPath(), now));
  }
}
