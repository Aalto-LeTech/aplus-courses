package fi.aalto.cs.apluscourses.intellij.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import fi.aalto.cs.apluscourses.utils.ArrayUtil;
import fi.aalto.cs.apluscourses.utils.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScAnnotations;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPrimaryConstructor;
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScSimpleTypeElement;
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause;
import org.jetbrains.plugins.scala.lang.psi.impl.base.ScModifierListImpl;
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.templates.ScTemplateParentsImpl;

public class ScalaClassDeclaration {

  private final String className;
  private final String[] arguments;
  private final String hierarchy;
  private final String[] traits;
  private final List<String> parameterModifiers;
  private final List<String> parameterAnnotations;
  private final String typeParameters;

  /**
   * Constructor.
   */
  public ScalaClassDeclaration(String className, String[] arguments,
                               String hierarchy, String[] traitHierarchy, String typeParameters,
                               String[] parameterModifiers, String[] parameterAnnotations) {
    this.className = className;
    this.arguments = arguments;
    this.parameterModifiers = Arrays.asList(parameterModifiers);
    this.parameterAnnotations = Arrays.asList(parameterAnnotations);
    this.hierarchy = hierarchy;
    this.traits = traitHierarchy;
    this.typeParameters = typeParameters;
  }

  public boolean checkClassName(String className) {
    return this.className.equals(className);
  }

  /**
   * Checks if the class' constructor is correct.
   * @param element The ScPrimaryConstructor to be checked.
   * @return whether the constructor is in the desired state.
   */
  public boolean checkConstructor(Optional<PsiElement> element) {
    if (element.isPresent()) {
      ScPrimaryConstructor constructor = (ScPrimaryConstructor) element.get();
      PsiParameter[] parameters = constructor.parameterList().getParameters();
      List<String> params = new ArrayList<>();
      for (PsiParameter param: parameters) {
        if (!checkParameterModifiers(param, params.size())
            || !checkParameterAnnotationsList(param, params.size())) {
          return false;
        }
        if (isVal(param)) {
          params.add("val " + param.getName() + ":" + param.getType().getPresentableText());
        } else if (isVar(param)) {
          params.add("var " + param.getName() + ":" + param.getType().getPresentableText());
        } else {
          params.add(param.getName() + ":" + param.getType().getPresentableText());
        }
      }
      return Arrays.equals(arguments, params.toArray());
    }
    return  false;
  }

  /**
   * Checks the type parameters of the function.
   * Certain characters are removed. In the case of type parameters, some text from the PSI
   * comes in the form of [A, B] and so the brackets need to be removed.
   * @param clause The ScTypeParamClause to be checked
   * @return true if the type parameters are correct, false otherwise.
   */
  public boolean checkTypeParameters(Optional<PsiElement> clause) {
    if (clause.isPresent() && typeParameters.length() != 0) {
      ScTypeParamClause typeParamClause = (ScTypeParamClause) clause.get();
      return typeParameters.equals(
          typeParamClause.getText().replace(" ", ""));
    } else {
      return clause.isEmpty() && typeParameters.length() == 0;
    }
  }

  /**
   * Checks the hierachy of the class.
   * @param element The ScTemplateParentsImpl to be checked
   * @return true if the hierarchy is correct, false otherwise.
   */
  public boolean checkExtendsBlock(Optional<PsiElement> element) {
    if (element.isPresent()) {
      PsiElement[] children = element.get().getChildren();
      Optional<PsiElement> extendsElement = Arrays.stream(children).filter(
          ScTemplateParentsImpl.class::isInstance).findFirst();
      // Peculiarity: If 'extends' is written without specifying a class name
      // it is considered correct by Scala
      // (the keyword extends is not visible when traversing the Psi tree)
      if (extendsElement.isPresent()) {
        if (hierarchy != null && !hierarchy.isEmpty()) {
          //traits may not be present!
          children = extendsElement.get().getChildren();
          if (children.length > 0 && hierarchy.equals(children[0].getText())) {
            var hierarchies = Arrays.stream(children)
                .filter(ScSimpleTypeElement.class::isInstance)
                .map(PsiElement::getText)
                .collect(Collectors.toSet());
            return traits.length == 0 || hierarchies.equals(ArrayUtil.toSet(traits));
          }
          // The extends keyword should be present first:
          // ScConstructorInvocationImpl -> class hierarchy
          // The with order does not matter ScSimpleTypeElementImpl
        } else {
          return false;
        }
      } else {
        return !element.get().getText().startsWith("extends")
            && hierarchy.isEmpty() && traits.length == 0;
      }
    }
    return false;
  }

  /**
   * Checks the modifiers (sealed, implicit, etc.) of the parameters.
   * @param element The ScModifierListImpl to be checked
   * @param index the index indicating which parameter
   *              (in parameterModifiers) these modifiers refer to
   * @return true if the modifiers are correct, false otherwise.
   */
  public boolean checkParameterModifiers(PsiParameter element, int index) {
    PsiElement[] children = element.getChildren();
    Optional<PsiElement> modifiersElement = Arrays.stream(children).filter(
        ScModifierListImpl.class::isInstance).findFirst();
    if (modifiersElement.isPresent()) {
      ScModifierListImpl modifierList = (ScModifierListImpl) modifiersElement.get();
      Set<String> psiModifiers = ArrayUtil.toSet(StringUtil.getArrayOfTokens(
          modifierList.getText(), ' '));
      Set<String> configModifiers = ArrayUtil.toSet(
          StringUtil.getArrayOfTokens(parameterModifiers.get(index), ' '));
      return psiModifiers.equals(configModifiers);
    }
    return false;
  }

  /**
   * Checks the annotations of the parameters.
   * @param element The ScAnnotations to be checked
   * @param index the index indicating which parameter
   *              (in parameterAnnotations) these modifiers refer to
   * @return true if the annotations are correct, false otherwise.
   */
  public boolean checkParameterAnnotationsList(PsiParameter element, int index) {
    PsiElement[] children = element.getChildren();
    Optional<PsiElement> annotationsElement = Arrays.stream(children).filter(
        ScAnnotations.class::isInstance).findFirst();
    if (annotationsElement.isPresent()) {
      ScAnnotations annotationList = (ScAnnotations) annotationsElement.get();
      Set<String> psiAnnotations = ArrayUtil.toSet(
          StringUtil.getArrayOfTokens(annotationList.getText(), ' '));
      Set<String> configAnnotations = ArrayUtil.toSet(
          StringUtil.getArrayOfTokens(parameterAnnotations.get(index), ' '));
      return psiAnnotations.equals(configAnnotations);
    }
    return false;
  }

  private boolean isVar(PsiParameter param) {
    if (param.getChildren().length > 0) {
      Collection<PsiElement> elements = PsiUtil.getPsiElementsSiblings(param.getChildren()[0]);
      return elements.stream().anyMatch(elem -> elem instanceof LeafPsiElement
          && elem.getText().equals("var"));
    }
    return false;
  }

  private boolean isVal(PsiParameter param) {
    if (param.getChildren().length > 0) {
      Collection<PsiElement> elements = PsiUtil.getPsiElementsSiblings(param.getChildren()[0]);
      return elements.stream().anyMatch(elem -> elem instanceof LeafPsiElement
          && elem.getText().equals("val"));
    }
    return false;
  }

}
