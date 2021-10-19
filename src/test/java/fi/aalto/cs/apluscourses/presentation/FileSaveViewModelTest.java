package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.mock;

import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class FileSaveViewModelTest {

  @Test
  public void testFileSaveViewModel() {
    VirtualFile defaultPath = mock(VirtualFile.class);
    FileSaveViewModel viewModel = new FileSaveViewModel(
        "Test Title",
        "Test description",
        defaultPath,
        "TestName.zip"
    );
    Assert.assertEquals("The title is equal to the one given to the constructor",
        "Test Title", viewModel.getTitle());
    Assert.assertEquals("The description is equal to the one given to the constructor",
        "Test description", viewModel.getDescription());
    Assert.assertSame("The default directory is the one given to the constructor",
        defaultPath, viewModel.getDefaultDirectory());
    Assert.assertEquals("The default name is the one given to the constructor",
        "TestName.zip", viewModel.getDefaultName());
  }

}
