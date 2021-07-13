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
import org.jetbrains.annotations.Nullable;

public class TestActionListener extends IdeActionListener {

  private final String fileName;

  /**
   * Constructor.
   */
  public TestActionListener(ListenerCallback callback, Project project,
                            String[] action, @Nullable String fileName) {
    super(callback, project, action);
    this.fileName = fileName;
  }

  /**
   * Creates an instance of ClassDeclarationListener based on the provided arguments.
   */
  public static TestActionListener create(ListenerCallback callback, Project project,
                                          Arguments arguments) {
    return new TestActionListener(callback, project,
                arguments.getArrayOrThrow("actionNames"), arguments.getOrThrow("filePath"));
  }


  @Override
  public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext,
                                    @NotNull AnActionEvent event) {
    boolean complete = true;
    if (fileName != null && !fileName.isEmpty()) {
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
