package fi.aalto.cs.apluscourses.intellij.model;

import static java.lang.Thread.sleep;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.testFramework.HeavyPlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import org.junit.Ignore;
import org.junit.Test;

public class IntelliJModuleTest extends HeavyPlatformTestCase implements TestHelper {

  public static final String JSON = ".json";

  @Ignore
  @Test
  public void testHasLocalChangesReturnsTrue() throws IOException, InterruptedException {
    //  given
    File tempDirectory = FileUtilRt.createTempDirectory("first", "", true);
    File tempFileOne = FileUtilRt.createTempFile(tempDirectory, "test1", JSON, true);
    File tempFileTwo = FileUtilRt.createTempFile(tempDirectory, "test2", JSON, true);
    assertTrue(tempFileTwo.setLastModified(Instant.now().toEpochMilli()));
    long oneSecond = 1000L;
    sleep(PluginSettings.REASONABLE_DELAY_FOR_MODULE_INSTALLATION + oneSecond);

    long now = Instant.now().toEpochMilli();
    assertTrue(tempFileOne.setLastModified(now));

    Module module = new ModelExtensions.TestModule("first", new URL("http://firstURL"), "1", "0",
        ZonedDateTime.now());
    //  when & then
    assertTrue(module.hasLocalChanges());
  }

  @Ignore
  @Test
  public void testHasLocalChangesReturnsFalse() throws IOException {
    //  given
    File tempDirectory = FileUtilRt.createTempDirectory("second", "", true);
    File tempFileOne = FileUtilRt.createTempFile(tempDirectory, "test1", JSON, true, true);
    FileUtilRt.createTempFile(tempDirectory, "test2", JSON, true, true);

    long now = Instant.now().toEpochMilli();
    assertTrue(tempFileOne.setLastModified(now));

    Module module = new ModelExtensions.TestModule("second", new URL("http://secondURL"), "1", "0",
        ZonedDateTime.now());

    //  when & then
    assertFalse(module.hasLocalChanges());
  }
}
