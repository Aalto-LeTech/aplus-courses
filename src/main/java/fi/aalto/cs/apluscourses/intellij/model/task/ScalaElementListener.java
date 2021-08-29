package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;

public abstract class ScalaElementListener extends CodeListener {

  protected ScalaElementListener(ListenerCallback callback, Project project, String filePath) {
    super(callback, project, filePath);
  }

  @Override
  protected boolean checkPsiFile(@NotNull PsiFile psiFile) {
    var visitor = new Visitor();
    psiFile.accept(visitor);
    return visitor.getResult();
  }

  protected abstract boolean checkScalaElement(ScalaPsiElement element);

  protected class Visitor extends ScalaRecursiveElementVisitor {
    private volatile boolean result = false;

    @Override
    public void visitScalaElement(ScalaPsiElement element) {
      super.visitScalaElement(element);
      if (checkScalaElement(element)) {
        result = true;
      }
    }

    public boolean getResult() {
      return result;
    }
  }
}
