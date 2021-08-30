package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import fi.aalto.cs.apluscourses.intellij.psi.PsiUtil;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaAssignStatement;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScAssignment;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScInfixExpr;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression;


public class AssignStatementListener extends ScalaElementListener {
  
  private final ScalaAssignStatement scalaAssignStatement;
  
  protected AssignStatementListener(ListenerCallback callback, Project project, String filePath,
                                    String variableName, String[] valueTokens) {
    
    super(callback, project, filePath);
    scalaAssignStatement = new ScalaAssignStatement(variableName, valueTokens);
  }

  /**
   * A factory method.
   * @return A new instance of this class.
   */
  public static AssignStatementListener create(ListenerCallback callback, Project project, Arguments arguments) {
    return new AssignStatementListener(callback, project,
        arguments.getString("filePath"),
        arguments.getString("variableName"),
        arguments.getArray("valueTokens"));
  }

  @Override
  protected boolean checkScalaElement(ScalaPsiElement element) {
    if (!(element instanceof ScAssignment)) {
      return false;
    }
    PsiElement[] children = element.getChildren();
    if (children.length <= 0) {
      return false;
    }
    Optional<PsiElement> refExpr = Arrays.stream(element.getChildren())
        .filter(ScReferenceExpression.class::isInstance)
        .findFirst();
    if (!scalaAssignStatement.checkVariableName(refExpr)) {
      return false;
    }
    Optional<PsiElement> infixExpr = Arrays.stream(element.getChildren())
        .filter(ScInfixExpr.class::isInstance)
        .findFirst();
    PsiElement equals = PsiUtil.findNextNonEmptyPsiElement(refExpr);
    return scalaAssignStatement.checkEquals(equals) && scalaAssignStatement.checkInfixExpr(infixExpr);
  }
}
