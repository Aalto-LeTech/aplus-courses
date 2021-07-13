package fi.aalto.cs.apluscourses.intellij.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiWhiteSpace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause;
import org.jetbrains.plugins.scala.lang.psi.impl.statements.params.ScParametersImpl;

public class ScalaFunctionDefinition {

  private final String methodName;
  private final String[] arguments;
  private final String[] methodBody;
  private final String typeParameters;

  /**
   * Constructor.
   */
  public ScalaFunctionDefinition(String methodName, String[] arguments, String[] methodBody,
                                 String typeParameters) {
    this.methodName = methodName;
    this.arguments = arguments;
    this.methodBody = methodBody;
    this.typeParameters = typeParameters;
  }

  public boolean checkMethodName(ScFunctionDefinition element) {
    return methodName.equals(element.getName());
  }

  /**
   * Checks the type parameters of the function.
   * @param clause The ScTypeParametersClause to be checked.
   * @return true if the type parameters are correct, false otherwise.
   */
  public boolean checkScTypeParametersClause(Optional<PsiElement> clause) {
    if (clause.isPresent() && typeParameters.length() != 0) {
      ScTypeParamClause typeParamClause = (ScTypeParamClause) clause.get();
      return typeParameters.equals(typeParamClause.getText().replace(" ", ""));
    } else {
      return clause.isEmpty() && typeParameters.length() == 0;
    }
  }

  /**
   * Checks the parameters(arguments) of the function.
   * @param optParameters The ScParametersImpl to be checked
   * @return true if the parameters are correct, false otherwise
   */
  public boolean checkParameters(Optional<PsiElement> optParameters) {
    if (optParameters.isPresent()) {
      PsiParameter[] parameters = ((ScParametersImpl) optParameters.get()).getParameters();
      List<String> params = new ArrayList<>();
      for (PsiParameter param: parameters) {
        params.add(param.getName() + ":" + param.getType().getPresentableText());
      }
      return Arrays.equals(arguments, params.toArray());
    }
    return false;
  }

  /**
   * Check the body of the function.
   * @param children The PsiElement to derive the function body from
   * @return true if the body is correct, false otherwise
   */
  public boolean checkFunctionBody(PsiElement[] children) {
    PsiElement methodBodyParent = children[children.length - 1];
    if (methodBodyParent != null) {
      children = methodBodyParent.getChildren();
      Collection<String> totalElements = getPsiElementsSiblings(children[0]);
      List<String> args = Arrays.asList(methodBody.clone());
      return args.equals(totalElements);
    }
    return false;
  }


  private Collection<String> getPsiElementsSiblings(PsiElement methodElement) {
    List<String> elements = new ArrayList<>();
    PsiElement prevSibling = methodElement.getPrevSibling();
    PsiElement nextSibling = methodElement;
    while (prevSibling != null) {
      nextSibling = prevSibling;
      prevSibling = prevSibling.getPrevSibling();
    }

    while (nextSibling != null) {
      if (nextSibling instanceof PsiWhiteSpace) {
        nextSibling = nextSibling.getNextSibling();
        continue;
      } else if (nextSibling.getText().contains(" ") && nextSibling.getChildren().length > 0) {
        elements.addAll(getPsiElementsSiblings(nextSibling.getChildren()[0]));
      } else {
        elements.add(nextSibling.getText());
      }
      nextSibling = nextSibling.getNextSibling();
    }
    return elements;
  }

}
