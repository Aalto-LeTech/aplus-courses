package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogInOutActionTest {

  private AnActionEvent event;
  private CourseProject courseProject;
  private LogInOutAction action;

  /**
   * Called before each test.
   */
  @BeforeEach
  void setUp() throws Exception {
    event = mock(AnActionEvent.class);
    var project = mock(Project.class);
    when(event.getProject()).thenReturn(project);
    var presentation = new Presentation();
    when(event.getPresentation()).thenReturn(presentation);

    var course = new ModelExtensions.TestCourse("oe1");
    courseProject = new CourseProject(course,
        RepeatedTask.create(() -> {
        }),
        RepeatedTask.create(() -> {
        }),
        project,
        mock(Notifier.class));
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
  void testLogInOutAction() {
    action.update(event);
    Assertions.assertEquals("Log out", event.getPresentation().getText());
    Assertions.assertNotNull(courseProject.getAuthentication());
    action.actionPerformed(event);
    action.update(event);
    Assertions.assertEquals("Log in", event.getPresentation().getText());
    Assertions.assertNull(courseProject.getAuthentication());
  }
}
