package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import fi.aalto.cs.apluscourses.presentation.FileSaveViewModel;
import java.nio.file.InvalidPathException;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class FileSaveView implements Dialog {

  @NotNull
  private final FileSaveViewModel viewModel;

  @NotNull
  private final Project project;

  public FileSaveView(@NotNull FileSaveViewModel viewModel,
                      @NotNull Project project) {
    this.viewModel = viewModel;
    this.project = project;
  }

  @Override
  public boolean showAndGet() {
    FileSaverDescriptor descriptor
        = new FileSaverDescriptor(viewModel.getTitle(), viewModel.getDescription());

    while (true) {
      FileSaverDialog dialog = FileChooserFactory
          .getInstance()
          .createSaveFileDialog(descriptor, project);
      VirtualFileWrapper file = dialog.save(
          viewModel.getDefaultDirectory(), viewModel.getDefaultName());

      if (file != null) {
        try {
          viewModel.setPath(file.getFile().toPath());
          return true;
        } catch (InvalidPathException e) {
          JOptionPane.showMessageDialog(null,
              getAndReplaceText("ui.exportModule.invalidPath.message", e.getReason()),
              getText("ui.exportModule.invalidPath.title"),
              JOptionPane.ERROR_MESSAGE);
          continue;
        }
      }

      return false;
    }
  }
}
