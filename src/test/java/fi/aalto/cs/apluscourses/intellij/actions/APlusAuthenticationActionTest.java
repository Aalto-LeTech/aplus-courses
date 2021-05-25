package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.intellij.DialogHelper;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

public class APlusAuthenticationActionTest {

  Project project;
  AnActionEvent actionEvent;
  CourseProject courseProject;
  CourseProjectProvider courseProjectProvider;
  Course course;
  volatile String token;
  Notifier notifier;
  PasswordStorage.Factory passwordStorageFactory;
  PasswordStorage passwordStorage;
  String service = "https://example.com";
  DialogHelper<AuthenticationViewModel> dialog;
  Dialogs dialogs;
  APlusAuthenticationAction action;

  /**
   * Called before each test.
   */
  @Before
  public void setUp() throws Exception {
    project = mock(Project.class);
    actionEvent = mock(AnActionEvent.class);
    doReturn(project).when(actionEvent).getProject();



    course = new ModelExtensions.TestCourse("oe1");
    token = "secrets";

    courseProject = new CourseProject(course, new URL("http://localhost:8000"), project);
    courseProjectProvider = mock(CourseProjectProvider.class);
    doReturn(courseProject).when(courseProjectProvider).getCourseProject(project);

    dialog = new DialogHelper<>(viewModel -> {
      viewModel.setToken(token.toCharArray());
      viewModel.build();
      return true;
    });

    dialogs = new Dialogs();
    dialogs.register(AuthenticationViewModel.class, new DialogHelper.Factory<>(dialog, project));

    notifier = mock(Notifier.class);
    passwordStorageFactory = mock(PasswordStorage.Factory.class);
    passwordStorage = mock(PasswordStorage.class);
    doReturn(passwordStorage).when(passwordStorageFactory).create(service);
    action = new APlusAuthenticationAction(
        courseProjectProvider, dialogs, passwordStorageFactory, notifier);
  }

  @Test
  public void testActionPerformed() {
    action.actionPerformed(actionEvent);

    var authentication = courseProject.getAuthentication();
    assertNotNull(authentication);
    assertTrue(((TokenAuthentication) authentication).tokenEquals(token));
  }

  @Test
  public void testActionPerformedCancels() {
    dialogs.register(AuthenticationViewModel.class, (viewModel, none) -> () -> false);

    action.actionPerformed(actionEvent);

    assertNull(courseProject.getAuthentication());
  }

}
