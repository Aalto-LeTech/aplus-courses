package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.mock;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class ModuleSelectionViewModelTest {

  @Test
  public void testModuleSelectionViewModel() {
    ModuleSelectionViewModel viewModel = new ModuleSelectionViewModel(new Module[0], "Test text", mock(Project.class));
    Assert.assertEquals("The view model has the info text given to the constructor",
        "Test text", viewModel.getInfoText());
  }

}
