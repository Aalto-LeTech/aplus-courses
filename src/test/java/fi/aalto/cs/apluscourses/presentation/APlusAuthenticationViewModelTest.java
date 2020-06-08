package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import org.junit.Assert;
import org.junit.Test;

public class APlusAuthenticationViewModelTest {

  @Test
  public void testAPlusAuthenticationViewModel() {
    Project project = mock(Project.class);
    doReturn("hello there").when(project).getBasePath();
    APlusAuthenticationViewModel viewModel = new APlusAuthenticationViewModel(project);

    Assert.assertEquals("The project should be equal to the one given to the constructor",
        "hello there", viewModel.getProject().getBasePath());
    Assert.assertNull(viewModel.getAuthentication());

    viewModel.setToken(new char[]{'a', 's', 'd'});

    Assert.assertArrayEquals("The view model should have an authentication instance",
        new char[]{'a', 's', 'd'}, viewModel.getAuthentication().getToken());
  }

}
