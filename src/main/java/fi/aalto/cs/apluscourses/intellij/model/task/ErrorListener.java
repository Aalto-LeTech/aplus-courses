package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.analysis.problemsView.ProblemsCollector;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ErrorListener extends CodeListener {

  private final ProblemsCollector problemsCollector;

  public ErrorListener(@NotNull ListenerCallback callback,
                       @NotNull Project project,
                       @NotNull String fileName) {
    super(callback, project, fileName);
    problemsCollector = ProblemsCollector.getInstance(project);
  }

  /**
   * Creates an instance of ErrorListener based on the provided arguments.
   */
  public static ErrorListener create(@NotNull ListenerCallback callback,
                                     @NotNull Project project,
                                     @NotNull Arguments arguments) {
    return new ErrorListener(callback, project, arguments.getString("filePath"));
  }


  @Override
  protected void checkPsiFile(@Nullable PsiFile file) {
    if (file == null || file.getVirtualFile() == null) {
      return;
    }

    if (problemsCollector.getFileProblemCount(file.getVirtualFile()) == 0) {
      callback.callback();
      isCorrect.set(true);
    }
  }

}