package fi.aalto.cs.apluscourses.intellij.activities;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class InitializationActivityTest extends BasePlatformTestCase {

  @Test
  public void testIsAPlusProjectReturnsFalseForNonAPlusProject() {
    assertFalse("The project is not an A+ project if there is no 'a-plus-project.json' file.",
        InitializationActivity.isAPlusProject(getProject()));
  }

  @Test
  public void testIsAPlusProjectReturnsTrueForAPlusProject() throws IOException {
    FileUtilRt.createTempFile(new File(getProject().getBasePath() + "/.idea"),
        "a-plus-project",
        ".json",
        true,
        true);

    assertTrue("The project an A+ project if the 'a-plus-project.json' file exists in the scope.",
        InitializationActivity.isAPlusProject(getProject()));
  }
}