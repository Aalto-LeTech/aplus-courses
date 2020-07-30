package fi.aalto.cs.apluscourses.presentation.commands;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.dialogs.TestDialogs;
import fi.aalto.cs.apluscourses.presentation.messages.Messenger;
import org.junit.Before;
import org.junit.Test;

public class APlusAuthenticationCommandTest {

  TestDialogs dialogs;
  TestDialogs.FactoryImpl<AuthenticationViewModel> dialogFactory;
  MainViewModel mainViewModel;
  Course course;
  String service;
  volatile String token;
  PasswordStorage passwordStorage;
  PasswordStorage.Factory passwordStorageFactory;
  APlusAuthenticationCommand command;
  MainViewModelContext context;
  Messenger messenger;

  /**
   * Set up before each test.
   */
  @Before
  public void setUp() {
    course = new ModelExtensions.TestCourse("oe1", "OE1");
    mainViewModel = new MainViewModel();
    mainViewModel.courseViewModel.set(new CourseViewModel(course));
    token = "secrets";
    dialogs = new TestDialogs();
    dialogFactory = new TestDialogs.FactoryImpl<>(
        viewModel -> {
          viewModel.setToken(token.toCharArray());
          return true;
        });
    dialogs.register(AuthenticationViewModel.class, dialogFactory);
    passwordStorage = mock(PasswordStorage.class);
    passwordStorageFactory = mock(PasswordStorage.Factory.class);
    doReturn(passwordStorage).when(passwordStorageFactory).create(service);
    messenger = mock(Messenger.class);

    command = new APlusAuthenticationCommand(passwordStorageFactory);
    context = mock(MainViewModelContext.class);
    doReturn(mainViewModel).when(context).getMainViewModel();
    doReturn(dialogs).when(context).getDialogs();
    doReturn(messenger).when(context).getMessenger();
  }

  @Test
  public void testActionPerformed() {
    command.execute(context);

    Authentication authentication = mainViewModel.authentication.get();
    assertNotNull(authentication);
    assertTrue(((TokenAuthentication) authentication).tokenEquals(token));
  }

  @Test
  public void testActionPerformedCancels() {
    dialogs.register(AuthenticationViewModel.class, (viewModel, none) -> () -> false);

    new APlusAuthenticationCommand(passwordStorageFactory).execute(context);

    assertNull(mainViewModel.authentication.get());
  }

}
