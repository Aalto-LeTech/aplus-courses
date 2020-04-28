package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.nio.file.Path;
import org.junit.Test;

public class APlusProjectTest extends BasePlatformTestCase {

  @Test
  public void testGetCourseFilePath() {
    //  given
    Project project = getProject();
    APlusProject aplusProject = new APlusProject(project);

    //  when
    Path courseFilePath = aplusProject.getCourseFilePath();

    //  then
    assertTrue("The course file paths is a valid one. (1)",
        courseFilePath.toString().contains("/tmp/unitTest_getBasePath"));
    assertTrue("The course file paths is a valid one. (2)",
        courseFilePath.toString().contains("/.idea/a-plus-project"));
  }

  @Test
  public void testGetBasePath() {
    //  given
    Project project = getProject();
    APlusProject aplusProject = new APlusProject(project);

    //  when
    Path basePath = aplusProject.getBasePath();

    //  then
    assertTrue("The bases paths is a valid one.",
        basePath.toString().contains("/tmp/unitTest_getBasePath"));
  }
}