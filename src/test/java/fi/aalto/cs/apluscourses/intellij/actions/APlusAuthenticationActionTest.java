package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.DialogHelper;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;

public class APlusAuthenticationActionTest {

  Project project;
  AnActionEvent actionEvent;

  /**
   * Called before each test.
   */
  @Before
  public void setUp() {
    project = mock(Project.class);
    actionEvent = mock(AnActionEvent.class);
    doReturn(project).when(actionEvent).getProject();
  }

  @Test
  public void testDefaultConstructor() {
    APlusAuthenticationAction action = new APlusAuthenticationAction();

    assertSame(PluginSettings.getInstance(), action.getMainViewModelProvider());
    assertSame(Dialogs.DEFAULT, action.getDialogs());
  }

  @Test
  public void testActionPerformed() {
    MainViewModel mainViewModel = new MainViewModel();
    MainViewModelProvider mainViewModelProvider = mock(MainViewModelProvider.class);
    doReturn(mainViewModel).when(mainViewModelProvider).getMainViewModel(project);

    String token = "secrets";

    DialogHelper<AuthenticationViewModel> dialog = new DialogHelper<>(viewModel -> {
      viewModel.setToken(token.toCharArray());
      return true;
    });

    Dialogs dialogs = new Dialogs();
    dialogs.register(AuthenticationViewModel.class, new DialogHelper.Factory<>(dialog, project));

    new APlusAuthenticationAction(mainViewModelProvider, dialogs).actionPerformed(actionEvent);

    ExerciseDataSource exerciseDataSource =
        Objects.requireNonNull(mainViewModel.getExerciseDataSource());

    assertTrue(((APlusAuthentication) exerciseDataSource.getAuthentication()).tokenEquals(token));
  }

  @Test
  public void testActionPerformedCancels() {
    MainViewModel mainViewModel = new MainViewModel();
    MainViewModelProvider mainViewModelProvider = mock(MainViewModelProvider.class);
    doReturn(mainViewModel).when(mainViewModelProvider).getMainViewModel(project);

    DialogHelper<AuthenticationViewModel> dialog = new DialogHelper<>(viewModel -> false);

    Dialogs dialogs = new Dialogs();
    dialogs.register(AuthenticationViewModel.class, new DialogHelper.Factory<>(dialog, project));

    new APlusAuthenticationAction(mainViewModelProvider, dialogs).actionPerformed(actionEvent);

    assertNull(mainViewModel.getExerciseDataSource());
  }

}
