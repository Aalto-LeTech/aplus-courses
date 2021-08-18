package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaMethodCall;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScArgumentExprList;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression;

import java.util.Arrays;
import java.util.Optional;

public class MethodCallListener extends CodeListener {
  
  private ScalaMethodCall scalaMethodCall;
  
  protected MethodCallListener(ListenerCallback callback, Project project, Arguments arguments) {
    super(callback, project, arguments.getString("filePath"));
    scalaMethodCall = new ScalaMethodCall(arguments.getString("methodName"), arguments.getArray("argsList"));
  }
  
  public static ActivitiesListener create(ListenerCallback callback, Project project, Arguments arguments) {
    return new MethodCallListener(callback, project, arguments);
  }
  
  @Override
  protected void checkPsiFile(@NotNull PsiFile psiFile) {
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScMethodCall) {
          PsiElement[] children = element.getChildren();
          Optional<PsiElement> refExprMethodName = Arrays.stream(children).filter(
              ScReferenceExpression.class::isInstance).findFirst();
          Optional<PsiElement> argsList = Arrays.stream(children).filter(
              ScArgumentExprList.class::isInstance).findFirst();
          if (scalaMethodCall.checkMethodName(refExprMethodName)
              && scalaMethodCall.checkArguments(argsList)) {
            ApplicationManager.getApplication().invokeLater(callback::callback);
          }
        }
      }
    });
  }
}
