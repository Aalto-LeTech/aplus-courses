package fi.aalto.cs.apluscourses.presentation;

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
    Project project = mock(Project.class);
    doReturn("hello there").when(project).getBasePath();

    AuthenticationViewModel viewModel = new AuthenticationViewModel(APlusTokenAuthentication::new);

    char[] token = new char[] {'a', 's', 'd'};
    viewModel.setToken(token);

    Authentication authentication = viewModel.build();

    assertTrue(authentication instanceof APlusTokenAuthentication);
    assertTrue("build() creates a correct object", ((TokenAuthentication)authentication)
        .tokenEquals("asd"));
  }

}
