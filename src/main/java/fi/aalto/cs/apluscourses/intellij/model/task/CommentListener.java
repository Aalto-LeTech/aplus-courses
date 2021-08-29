package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public class CommentListener extends CodeListener {
  
  private final String text;
  
  protected CommentListener(ListenerCallback callback,
                            Project project,
                            String filePath,
                            String text) {
    super(callback, project, filePath);
    this.text = text;
  }
  
  public static CodeListener create(ListenerCallback listenerCallback,
                                    Project project,
                                    Arguments arguments) {
    return new CommentListener(listenerCallback, project,
        arguments.getString("filePath"),
        arguments.getString("text"));
  }
  
  @Override
  protected boolean checkPsiFile(@NotNull PsiFile psiFile) {
    return psiFile.getText().contains("//" + text);
  }
}
