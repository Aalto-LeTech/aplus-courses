package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.module.Module;
import org.junit.Assert;
import org.junit.Test;

public class ModuleSelectionViewModelTest {

  @Test
  public void testModuleSelectionViewModel() {
    ModuleSelectionViewModel viewModel = new ModuleSelectionViewModel(new Module[0], "Test text");
    Assert.assertEquals("The view model has the info text given to the constructor",
        "Test text", viewModel.getInfoText());
  }

}
