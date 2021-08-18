package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.intellij.psi.NewObjectAssignment;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPatternList;
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScSimpleTypeElement;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScArgumentExprList;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScNewTemplateDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScPatternDefinition;


public class NewObjectAssignmentListener extends CodeListener {

  private final NewObjectAssignment newObjectAssignment;

  protected NewObjectAssignmentListener(ListenerCallback callback, Project project,
                                        String filePath,
                                        String variableName,
                                        String variableType,
                                        String className,
                                        String[] argsList) {
    super(callback, project, filePath);
    this.newObjectAssignment = new NewObjectAssignment(variableName, variableType, className, argsList);
  }
  
  /**
   * Factory method.
   */
  public static NewObjectAssignmentListener create(ListenerCallback callback,
                                                   Project project, Arguments arguments) {
    return new NewObjectAssignmentListener(callback, project, arguments.getString("filePath"),
            arguments.getString("variableName"),
            arguments.getString("variableType"),
            arguments.getString("className"),
            arguments.getArray("argsList"));
  }

  @Override
  protected void checkPsiFile(@NotNull PsiFile psiFile) {
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScPatternDefinition) {
          PsiElement[] children = element.getChildren();
          Optional<PsiElement> templateDef = Arrays.stream(children).filter(
              ScNewTemplateDefinition.class::isInstance).findFirst();
          Optional<PsiElement> patternList = Arrays.stream(children).filter(
              ScPatternList.class::isInstance).findFirst();
          if (newObjectAssignment.checkVariableType(patternList)
              && newObjectAssignment.checkName(patternList)) {
            //TODO check if these lines above till here are the same and put into a super class! the subclasses will only check the different types of values assigned!!
            //If the keyword new is missing, ExtendsBlock does not exist in the PSI
            //and the command is treated like a MethodCall.
            Optional<PsiElement> constructorInvocation = newObjectAssignment.traversePsiTree(templateDef);
            if (constructorInvocation.isPresent()) {
              children = constructorInvocation.get().getChildren();
              Optional<PsiElement> simpleType = Arrays.stream(children).filter(
                  ScSimpleTypeElement.class::isInstance).findFirst();
              Optional<PsiElement> argList = Arrays.stream(children).filter(
                  ScArgumentExprList.class::isInstance).findFirst();
              if (newObjectAssignment.checkNewObjectAssignment(simpleType, argList)) {
                  ApplicationManager.getApplication().invokeLater(callback::callback);
                  isCorrect.set(true);
              }
            }
          }
        }
      }
    });
  }
}