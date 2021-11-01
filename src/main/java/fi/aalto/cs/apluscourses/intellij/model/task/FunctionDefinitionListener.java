package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import fi.aalto.cs.apluscourses.intellij.psi.PsiUtil;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaFunctionDefinition;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause;
import org.jetbrains.plugins.scala.lang.psi.impl.statements.params.ScParametersImpl;


public class FunctionDefinitionListener extends ScalaElementListener {

  private final ScalaFunctionDefinition scalaFunctionDefinition;

  /**
   * Constructor.
   */
  public FunctionDefinitionListener(@NotNull ListenerCallback callback,
                                    @NotNull Project project,
                                    @NotNull String methodName,
                                    @NotNull String[] arguments,
                                    @NotNull String[] body,
                                    @NotNull String typeParameters,
                                    @NotNull String filePath,
                                    boolean checkEquals) {
    super(callback, project, filePath);
    this.scalaFunctionDefinition = new ScalaFunctionDefinition(methodName,
        arguments, body, typeParameters, checkEquals);
  }

  /**
   * Creates an instance of ClassDeclarationListener based on the provided arguments.
   */
  public static FunctionDefinitionListener create(ListenerCallback callback,
                                                  Project project, Arguments arguments) {
    return new FunctionDefinitionListener(callback, project,
        arguments.getString("methodName"),
        arguments.getArray("methodArguments"),
        arguments.getArray("methodBody"),
        arguments.getString("typeParamClause"),
        arguments.getString("filePath"),
        Boolean.getBoolean(arguments.getString("checkEquals")));
  }

  @Override
  protected boolean checkScalaElement(ScalaPsiElement element) {
    if (!(element instanceof ScFunctionDefinition)
        || !scalaFunctionDefinition.checkMethodName((ScFunctionDefinition) element)) {
      return false;
    }
    PsiElement[] children = element.getChildren();
    Optional<PsiElement> optTypeParameters = Arrays.stream(children).filter(
        ScTypeParamClause.class::isInstance).findFirst();
    Optional<PsiElement> optParameters = Arrays.stream(children).filter(
        ScParametersImpl.class::isInstance).findFirst();
    return scalaFunctionDefinition.checkScTypeParametersClause(optTypeParameters)
        && scalaFunctionDefinition.checkParameters(optParameters)
        && scalaFunctionDefinition.checkFunctionBody(children)
        && !PsiUtil.psiNextSiblingHasErrors(element);
  }

}
