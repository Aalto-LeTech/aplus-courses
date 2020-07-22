package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import org.junit.Test;

public class AuthenticationViewModelTest {

  @Test
  public void testAPlusAuthenticationViewModel() {
    Project project = mock(Project.class);
    doReturn("hello there").when(project).getBasePath();

    AuthenticationViewModel viewModel = new AuthenticationViewModel();

    char[] token = new char[] {'a', 's', 'd'};
    viewModel.setToken(token);

    Authentication authentication = viewModel.build();

    assertTrue(authentication instanceof APlusAuthentication);
    assertTrue("build() creates a correct object", ((APlusAuthentication)authentication)
        .tokenEquals("asd"));
  }

}
