package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaVariableDeclaration;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPatternList;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScPatternDefinition;

public class VariableDeclarationListener extends ScalaElementListener {
  
  private final ScalaVariableDeclaration scalaVariableDeclaration;
  
  protected VariableDeclarationListener(ListenerCallback callback, Project project,
                                        String filePath, String variableType, String variableName,
                                        String[] valueTokens) {
    super(callback, project, filePath);
    scalaVariableDeclaration = new ScalaVariableDeclaration(variableType, variableName, valueTokens);
  }

  /**
   * Factory method.
   * @return A new instance of the class.
   */
  public static VariableDeclarationListener create(ListenerCallback callback,
                                                   Project project,
                                                   Arguments arguments) {
    return new VariableDeclarationListener(callback, project,
        arguments.getString("filePath"),
        arguments.getString("variableType"),
        arguments.getString("variableName"),
        arguments.getArray("valueTokens"));
  }

  @Override
  protected boolean checkScalaElement(ScalaPsiElement element) {
    if (!(element instanceof ScPatternDefinition)) {
      return false;
    }
    PsiElement[] children = element.getChildren();
    Optional<PsiElement> refPattern = Arrays.stream(children).filter(
        ScPatternList.class::isInstance).findFirst();
    return scalaVariableDeclaration.checkVariableName(refPattern)
        && scalaVariableDeclaration.checkVariableType(refPattern)
        && scalaVariableDeclaration.checkAssignedValue(refPattern);
  }
}
