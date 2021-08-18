package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaComment;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public class CommentListener extends CodeListener {
  
  private final ScalaComment comment;
  private final String text;
  
  protected CommentListener(ListenerCallback callback, Project project, String filePath,
                            String text) {
    super(callback, project, filePath);
    comment = new ScalaComment(text);
    this.text = text;
  }
  
  public static CodeListener create(ListenerCallback listenerCallback, Project project,
                                    Arguments arguments) {
    return new CommentListener(listenerCallback, project, arguments.getString("filePath"),
      arguments.getString("text"));
  }
  
  @Override
  protected void checkPsiFile(@NotNull PsiFile psiFile) {
    //String checks
    //Comments are not traversed through the PSI
    if(psiFile.getText().contains("//" + text)) {
      ApplicationManager.getApplication().invokeLater(callback::callback);
    }
  }
}
