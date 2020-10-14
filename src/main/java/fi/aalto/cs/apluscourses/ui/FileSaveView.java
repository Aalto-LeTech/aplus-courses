package fi.aalto.cs.apluscourses.ui;

import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import fi.aalto.cs.apluscourses.presentation.FileSaveViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileSaveView implements Dialog {

  @NotNull
  private final FileSaveViewModel viewModel;

  @NotNull
  private final Project project;

  public FileSaveView(@NotNull FileSaveViewModel viewModel,
                      @Nullable Project project) {
    this.viewModel = viewModel;
    this.project = project;
  }

  @Override
  public boolean showAndGet() {
    FileSaverDescriptor descriptor
        = new FileSaverDescriptor(viewModel.getTitle(), viewModel.getDescription());
    FileSaverDialog dialog = FileChooserFactory
        .getInstance()
        .createSaveFileDialog(descriptor, project);
    VirtualFileWrapper file = dialog.save(
        viewModel.getDefaultDirectory(), viewModel.getDefaultName());
    if (file != null) {
      viewModel.setPath(file.getFile().toPath());
      return true;
    }
    return false;
  }
}