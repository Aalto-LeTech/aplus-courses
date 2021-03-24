package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.intellij.DialogHelper;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import org.junit.Before;
import org.junit.Test;

public class APlusAuthenticationActionTest extends BasePlatformTestCase {

  Project project;
  AnActionEvent actionEvent;
  MainViewModel mainViewModel;
  MainViewModelProvider mainViewModelProvider;
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
    super.setUp();
    project = mock(Project.class);
    actionEvent = mock(AnActionEvent.class);
    doReturn(project).when(actionEvent).getProject();

    mainViewModel = new MainViewModel(new Options());
    mainViewModelProvider = mock(MainViewModelProvider.class);
    doReturn(mainViewModel).when(mainViewModelProvider).getMainViewModel(project);

    course = new ModelExtensions.TestCourse("oe1");
    mainViewModel.courseViewModel.set(new CourseViewModel(course));
    token = "secrets";

    dialog = new DialogHelper<>(viewModel -> {
      viewModel.setToken(token.toCharArray());
      return true;
    });

    dialogs = new Dialogs();
    dialogs.register(AuthenticationViewModel.class, new DialogHelper.Factory<>(dialog, project));

    notifier = mock(Notifier.class);
    passwordStorageFactory = mock(PasswordStorage.Factory.class);
    passwordStorage = mock(PasswordStorage.class);
    doReturn(passwordStorage).when(passwordStorageFactory).create(service);
    action = new APlusAuthenticationAction(
        mainViewModelProvider, dialogs, passwordStorageFactory, notifier);
  }

  @Test
  public void ignoretestDefaultConstructor() {
    APlusAuthenticationAction action = new APlusAuthenticationAction();

    assertSame(PluginSettings.getInstance(), action.getMainViewModelProvider());
    assertSame(Dialogs.DEFAULT, action.getDialogs());
  }

  @Test
  public void testActionPerformed() {
    action.actionPerformed(actionEvent);

    Authentication authentication = mainViewModel.authentication.get();
    assertNotNull(authentication);
    assertTrue(((TokenAuthentication) authentication).tokenEquals(token));
  }

  @Test
  public void testActionPerformedCancels() {
    dialogs.register(AuthenticationViewModel.class, (viewModel, none) -> () -> false);

    action.actionPerformed(actionEvent);

    assertNull(mainViewModel.authentication.get());
  }

}
