package fi.aalto.cs.apluscourses.intellij.model;

import static java.lang.Thread.sleep;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.testFramework.HeavyPlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Module;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import org.junit.Test;

public class IntelliJModuleTest extends HeavyPlatformTestCase implements TestHelper {

  @Test
  public void testHasLocalChangesReturnsTrue() throws IOException, InterruptedException {
    //  given
    Project project = getProject();

    IntelliJModelFactory factory = new IntelliJModelFactory(project);
    Module module = factory.createModule("first", new URL("http://firstURL"), "1");
    IntelliJModule intelliJModule = (IntelliJModule) module;

    File tempDirectory = FileUtilRt.createTempDirectory("first", "", true);
    File tempFileOne = FileUtilRt.createTempFile(tempDirectory, "test1", ".json", true);
    File tempFileTwo = FileUtilRt.createTempFile(tempDirectory, "test2", ".json", true);
    tempFileTwo.setLastModified(Instant.now().toEpochMilli());
    long oneSecond = 1000L;
    sleep(PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION + oneSecond);

    long now = Instant.now().toEpochMilli();
    tempFileOne.setLastModified(now);

    //  when & then
    assertTrue(intelliJModule.hasLocalChanges());
  }

  @Test
  public void testHasLocalChangesReturnsFalse() throws IOException {
    //  given
    Project project = getProject();

    IntelliJModelFactory factory = new IntelliJModelFactory(project);
    Module module = factory.createModule("second", new URL("http://secondURL"), "1");
    IntelliJModule intelliJModule = (IntelliJModule) module;

    File tempDirectory = FileUtilRt.createTempDirectory("second", "", true);
    File tempFileOne = FileUtilRt.createTempFile(tempDirectory, "test1", ".json", true, true);
    File tempFileTwo = FileUtilRt.createTempFile(tempDirectory, "test2", ".json", true, true);

    long now = Instant.now().toEpochMilli();
    tempFileOne.setLastModified(now);

    //  when & then
    assertFalse(intelliJModule.hasLocalChanges());
  }
}