package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaMethodCall;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScArgumentExprList;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression;


public class MethodCallListener extends ScalaElementListener {

  private final ScalaMethodCall scalaMethodCall;

  protected MethodCallListener(ListenerCallback callback,
                               Project project,
                               String filePath,
                               String methodName,
                               String[] argsList) {
    super(callback, project, filePath);
    scalaMethodCall = new ScalaMethodCall(methodName, argsList);
  }

  /**
   * Factory method.
   *
   * @return A new instance of this class.
   */
  public static ActivitiesListener create(ListenerCallback callback, Project project, Arguments arguments) {
    return new MethodCallListener(callback, project,
        arguments.getString("filePath"),
        arguments.getString("methodName"),
        arguments.getArray("argsList"));
  }

  @Override
  protected boolean checkScalaElement(ScalaPsiElement element) {
    if (!(element instanceof ScMethodCall)) {
      return false;
    }
    PsiElement[] children = element.getChildren();
    Optional<PsiElement> refExprMethodName = Arrays.stream(children).filter(
        ScReferenceExpression.class::isInstance).findFirst();
    Optional<PsiElement> argsList = Arrays.stream(children).filter(
        ScArgumentExprList.class::isInstance).findFirst();
    return scalaMethodCall.checkMethodName(refExprMethodName)
        && scalaMethodCall.checkArguments(argsList);
  }
}
