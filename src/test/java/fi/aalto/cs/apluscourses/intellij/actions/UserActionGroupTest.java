package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

public class UserActionGroupTest {
  private AnActionEvent event;
  private CourseProject courseProject;
  private UserActionGroup action;

  /**
   * Called before each test.
   */
  @Before
  public void setUp() throws Exception {
    event = mock(AnActionEvent.class);
    var project = mock(Project.class);
    when(event.getProject()).thenReturn(project);
    var presentation = new Presentation();
    when(event.getPresentation()).thenReturn(presentation);

    var course = new ModelExtensions.TestCourse("oe1");
    courseProject = new CourseProject(course, new URL("http://localhost:8000"), project);
    var courseProjectProvider = mock(CourseProjectProvider.class);
    when(courseProjectProvider.getCourseProject(project)).thenReturn(courseProject);

    action = new UserActionGroup(courseProjectProvider);
  }

  @Test
  public void testUserActionGroup() {
    action.update(event);
    assertEquals("Not logged in", event.getPresentation().getText());
    var authentication = mock(Authentication.class);
    courseProject.setAuthentication(authentication);
    action.update(event);
    assertEquals("Logged in as test", event.getPresentation().getText());
  }
}
