package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import java.time.ZonedDateTime;
import org.junit.Test;

public class ModuleVirtualFileVisitorTest {

  @Test
  public void testVisitFileReturnsTrue() {
    //  given
    ZonedDateTime now = ZonedDateTime.now();
    ModuleVirtualFileVisitor moduleVirtualFileVisitor = new ModuleVirtualFileVisitor(now);
    VirtualFile virtualFile = new LightVirtualFile();
    VirtualFile spyVirtualFile = spy(virtualFile);
    assertFalse(moduleVirtualFileVisitor.isHasChanges());
    long later = now.plusSeconds(20L).toInstant().toEpochMilli();
    when(spyVirtualFile.getTimeStamp()).thenReturn(later);

    //  when
    boolean result = moduleVirtualFileVisitor.visitFile(spyVirtualFile);

    //  then
    assertTrue(moduleVirtualFileVisitor.isHasChanges());
    assertFalse(result);
  }

  @Test
  public void testVisitFileReturnsFalse() {
    //  given
    ModuleVirtualFileVisitor moduleVirtualFileVisitor = new ModuleVirtualFileVisitor(
        ZonedDateTime.now());
    VirtualFile virtualFile = new LightVirtualFile();
    assertFalse(moduleVirtualFileVisitor.isHasChanges());

    //  when
    boolean result = moduleVirtualFileVisitor.visitFile(virtualFile);

    //  then
    assertFalse(moduleVirtualFileVisitor.isHasChanges());
    assertTrue(result);
  }
}