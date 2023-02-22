package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.mock;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModuleSelectionViewModelTest {

  @Test
  void testModuleSelectionViewModel() {
    ModuleSelectionViewModel viewModel = new ModuleSelectionViewModel(new Module[0], "Test text", mock(Project.class));
    Assertions.assertEquals("Test text", viewModel.getInfoText(),
        "The view model has the info text given to the constructor");
  }

}
