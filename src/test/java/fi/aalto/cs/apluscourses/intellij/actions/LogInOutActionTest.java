package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

public class LogInOutActionTest {

  private AnActionEvent event;
  private CourseProject courseProject;
  private LogInOutAction action;

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

    var authentication = mock(Authentication.class);
    courseProject.setAuthentication(authentication);

    var passwordStorageFactory = mock(PasswordStorage.Factory.class);
    var passwordStorage = mock(PasswordStorage.class);
    doReturn(passwordStorage).when(passwordStorageFactory).create("https://example.com");

    action = new LogInOutAction(courseProjectProvider, passwordStorageFactory);
  }

  @Test
  public void testLogInOutAction() {
    action.update(event);
    assertEquals("Log out", event.getPresentation().getText());
    assertNotNull(courseProject.getAuthentication());
    action.actionPerformed(event);
    action.update(event);
    assertEquals("Log in", event.getPresentation().getText());
    assertNull(courseProject.getAuthentication());
  }
}
