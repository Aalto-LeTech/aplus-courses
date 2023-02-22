package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AuthenticationViewModelTest {

  @Test
  void testAPlusAuthenticationViewModel() {
    Project project = mock(Project.class);
    doReturn("hello there").when(project).getBasePath();
    ExerciseDataSource dataSource = mock(ExerciseDataSource.class);

    AuthenticationViewModel viewModel = new AuthenticationViewModel(
        APlusTokenAuthentication::new,
        "https://example.com",
        dataSource
    );

    char[] token = new char[] {'a', 's', 'd'};
    viewModel.setToken(token);

    viewModel.build();
    Authentication authentication = viewModel.getAuthentication();

    Assertions.assertTrue(authentication instanceof APlusTokenAuthentication);
    Assertions.assertEquals("https://example.com", viewModel.getAuthenticationHtmlUrl(),
        "The view model has the URL passed to the constructor");
    Assertions.assertTrue(((TokenAuthentication) authentication)
        .tokenEquals("asd"), "build() creates a correct object");
  }

}
