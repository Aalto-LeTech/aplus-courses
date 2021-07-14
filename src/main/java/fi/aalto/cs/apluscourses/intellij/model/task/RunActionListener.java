package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public class RunActionListener extends IdeActionListener {

  private final String fileName;

  /**
   * Constructor.
   */
  public RunActionListener(@NotNull ListenerCallback callback,
                           @NotNull Project project,
                           @NotNull String[] actionNames,
                           @NotNull String fileName) {
    super(callback, project, actionNames);
    this.fileName = fileName;
  }

  /**
   * Creates an instance of ClassDeclarationListener based on the provided arguments.
   */
  public static RunActionListener create(ListenerCallback callback, Project project,
                                         Arguments arguments) {
    return new RunActionListener(callback, project,
                arguments.getArray("actionNames"), arguments.getString("filePath"));
  }


  @Override
  public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext,
                                    @NotNull AnActionEvent event) {
    boolean complete = true;
    if (!fileName.isEmpty()) {
      String filePath = project.getBasePath() + fileName;
      VirtualFile file = event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE);
      complete = file != null && filePath.equals(file.getPath());
    }
    if ((complete && actionNames.contains(action.getTemplateText()))
                    || actionNames.contains(event.getPresentation().getText())) {
      callback.callback();
    }
  }
}
