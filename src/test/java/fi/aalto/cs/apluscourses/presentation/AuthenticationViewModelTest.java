package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import org.junit.Test;

public class AuthenticationViewModelTest {

  @Test
  public void testAPlusAuthenticationViewModel() {
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

    assertTrue(authentication instanceof APlusTokenAuthentication);
    assertEquals("The view model has the URL passed to the constructor",
        "https://example.com", viewModel.getAuthenticationHtmlUrl());
    assertTrue("build() creates a correct object", ((TokenAuthentication)authentication)
        .tokenEquals("asd"));
  }

}
