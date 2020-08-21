package fi.aalto.cs.apluscourses.ui.courseproject;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import org.jetbrains.annotations.NotNull;

public class CourseProjectActionDialogsImpl implements CourseProjectActionDialogs {
  @Override
  public boolean showMainDialog(@NotNull Project project,
                                @NotNull CourseProjectViewModel courseProjectViewModel) {
    CourseProjectView dialog = new CourseProjectView(project, courseProjectViewModel);
    return dialog.showAndGet();
  }

  @Override
  public boolean showRestartDialog() {
    return Messages.showOkCancelDialog(
        getText("ui.courseProject.dialogs.showRestartDialog.message"),
        getText("ui.courseProject.dialogs.showRestartDialog.title"),
        getText("ui.courseProject.dialogs.showRestartDialog.okText"),
        getText("ui.courseProject.dialogs.showRestartDialog.cancelText"),
        Messages.getQuestionIcon()) == Messages.OK;
  }
}
