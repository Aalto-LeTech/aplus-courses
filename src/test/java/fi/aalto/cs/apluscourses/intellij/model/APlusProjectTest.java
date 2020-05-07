package fi.aalto.cs.apluscourses.intellij.model;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class APlusProjectTest {

  @Test
  public void testGetCourseFilePath() {
    Project project = mock(Project.class);
    doReturn("test").when(project).getBasePath();

    APlusProject aplusProject = new APlusProject(project);

    Assert.assertEquals("The course file path should be correct",
        Paths.get("test", Project.DIRECTORY_STORE_FOLDER, "a-plus-project"),
        aplusProject.getCourseFilePath());
  }

  @Test
  public void testGetBasePath() {
    Project project = mock(Project.class);
    doReturn(".idea").when(project).getBasePath();

    APlusProject aplusProject = new APlusProject(project);

    Assert.assertEquals("The base path should be correct",
        Paths.get(Project.DIRECTORY_STORE_FOLDER),
        aplusProject.getBasePath());
  }
}