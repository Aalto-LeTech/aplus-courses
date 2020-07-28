package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import org.junit.Test;

public class AuthenticationViewModelTest {

  @Test
  public void testAPlusAuthenticationViewModel() {
    char[] token = new char[] {'a', 's', 'd'};
    TokenAuthentication authentication = mock(TokenAuthentication.class);
    TokenAuthentication.Factory factory = mock(TokenAuthentication.Factory.class);
    doReturn(authentication).when(factory).create(token);

    AuthenticationViewModel viewModel = new AuthenticationViewModel(factory);

    viewModel.setToken(token);

    assertSame(authentication, viewModel.build());
  }

}
