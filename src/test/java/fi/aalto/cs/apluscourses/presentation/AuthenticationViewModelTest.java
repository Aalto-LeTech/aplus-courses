package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.utils.Event;
import org.junit.Test;

public class AuthenticationViewModelTest {

  @Test
  public void testAPlusAuthenticationViewModel() {
    Project project = mock(Project.class);
    doReturn("hello there").when(project).getBasePath();

    Authentication authentication = new APlusAuthentication(15);
    AuthenticationViewModel viewModel = new AuthenticationViewModel(authentication);

    assertEquals(15, viewModel.getMaxLength());
    assertFalse(viewModel.isSet());

    Object listener = new Object();
    Event.Callback<Object> callback = mock(Event.Callback.class);
    doCallRealMethod().when(callback).callbackUntyped(any());

    viewModel.changed.addListener(listener, callback);

    char[] token = new char[] {'a', 's', 'd'};
    viewModel.setToken(token);

    verify(callback).callback(listener);

    assertTrue(authentication.isSet());
    assertTrue(viewModel.isSet());

    assertNotNull(listener);
  }

}
