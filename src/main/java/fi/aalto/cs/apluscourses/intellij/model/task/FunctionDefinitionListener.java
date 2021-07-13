package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaFunctionDefinition;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause;
import org.jetbrains.plugins.scala.lang.psi.impl.statements.params.ScParametersImpl;


public class FunctionDefinitionListener extends ScalaCodeListener {

  private final ScalaFunctionDefinition scalaFunctionDefinition;

  /**
   * Constructor.
   */
  public FunctionDefinitionListener(ListenerCallback callback,
                                    Project project, String methodName, String[] arguments,
                                    String[] body, String typeParameters, String filePath) {
    super(callback, project, filePath);
    this.scalaFunctionDefinition = new ScalaFunctionDefinition(methodName,
        arguments, body, typeParameters);
  }

  /**
   * Creates an instance of ClassDeclarationListener based on the provided arguments.
   */
  public static FunctionDefinitionListener create(ListenerCallback callback,
                                                  Project project, Arguments arguments) {
    return new FunctionDefinitionListener(callback, project,
                arguments.getOrThrow("methodName"),
                arguments.getArrayOrThrow("methodArguments"),
                arguments.getArrayOrThrow("methodBody"),
                arguments.getOrThrow("typeParamClause"),
                arguments.getOrThrow("filePath"));
  }


  @Override
  protected void checkPsiFile(@Nullable PsiFile psiFile) {
    if (psiFile == null) {
      return;
    }
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScFunctionDefinition
                && scalaFunctionDefinition.checkMethodName((ScFunctionDefinition) element)) {
          PsiElement[] children = element.getChildren();
          Optional<PsiElement> optTypeParameters = Arrays.stream(children).filter(
              ScTypeParamClause.class::isInstance).findFirst();
          Optional<PsiElement> optParameters = Arrays.stream(children).filter(
              ScParametersImpl.class::isInstance).findFirst();
          if (scalaFunctionDefinition.checkScTypeParametersClause(optTypeParameters)
               && scalaFunctionDefinition.checkParameters(optParameters)
                && scalaFunctionDefinition.checkFunctionBody(children)) {
            ApplicationManager.getApplication().invokeLater(callback::callback);
            isCorrect.set(true);
          }
        }
      }
    });
  }

}
