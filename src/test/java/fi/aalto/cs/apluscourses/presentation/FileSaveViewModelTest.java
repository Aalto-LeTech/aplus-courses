package fi.aalto.cs.apluscourses.presentation;

import static org.mockito.Mockito.mock;

import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FileSaveViewModelTest {

  @Test
  void testFileSaveViewModel() {
    VirtualFile defaultPath = mock(VirtualFile.class);
    FileSaveViewModel viewModel = new FileSaveViewModel(
        "Test Title",
        "Test description",
        defaultPath,
        "TestName.zip"
    );
    Assertions.assertEquals("Test Title", viewModel.getTitle(),
        "The title is equal to the one given to the constructor");
    Assertions.assertEquals("Test description", viewModel.getDescription(),
        "The description is equal to the one given to the constructor");
    Assertions.assertSame(defaultPath, viewModel.getDefaultDirectory(),
        "The default directory is the one given to the constructor");
    Assertions.assertEquals("TestName.zip", viewModel.getDefaultName(),
        "The default name is the one given to the constructor");
  }

}
