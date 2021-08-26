package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaVariableDeclaration;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPatternList;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScPatternDefinition;

public class VariableDeclarationListener extends CodeListener {
  
  private ScalaVariableDeclaration scalaVariableDeclaration;
  
  protected VariableDeclarationListener(ListenerCallback callback, Project project,
                                        String filePath, String variableType, String variableName,
                                        String[] valueTokens) {
    super(callback, project, filePath);
    scalaVariableDeclaration = new ScalaVariableDeclaration(variableType, variableName, valueTokens);
  }
  
  public static VariableDeclarationListener create(ListenerCallback callback, Project project,
                                                   Arguments arguments) {
    return new VariableDeclarationListener(callback, project, arguments.getString("filePath"),
      arguments.getString("variableType"), arguments.getString("variableName"), arguments.getArray("valueTokens"));
  }
  
  @Override
  protected void checkPsiFile(@NotNull PsiFile psiFile) {
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScPatternDefinition) {
          PsiElement[] children = element.getChildren();
          Optional<PsiElement> refPattern = Arrays.stream(children).filter(
              ScPatternList.class::isInstance).findFirst();
          if (scalaVariableDeclaration.checkVariableName(refPattern)
              && scalaVariableDeclaration.checkVariableType(refPattern)
              && scalaVariableDeclaration.checkAssignedValue(refPattern)) {
            ApplicationManager.getApplication().invokeLater(callback::callback);
          }
        }
      }
    });
  }
}
