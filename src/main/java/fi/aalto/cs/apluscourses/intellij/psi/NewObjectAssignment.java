package fi.aalto.cs.apluscourses.intellij.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScConstructorInvocation;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPatternList;
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern;
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScSimpleTypeElement;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScArgumentExprList;
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.templates.ScExtendsBlock;
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.templates.ScTemplateParents;


public class NewObjectAssignment {
  private final String variableName;
  private final String variableType;
  private final String className;
  private final String[] argsList;
  
  /**
   * Constructor.
   */
  public NewObjectAssignment(String variableName, String variableType, String className,
                             String[] argsList) {
    this.variableName = variableName;
    this.variableType = variableType;
    this.className = className;
    this.argsList = argsList;
  }
  
  /**
   * Checks if the variable is val or var.
   */
  public boolean checkVariableType(Optional<PsiElement> element) {
    if (element.isPresent()) {
      ScPatternList patternList = (ScPatternList) element.get();
      Collection<PsiElement> allChildren = PsiUtil.getPsiElementsSiblings(patternList);
  
      return allChildren.stream().anyMatch(elem -> elem instanceof LeafPsiElement
            && elem.getText().equals(variableType));
    }
    return false;
  }
  
  /**
   * Traverse the PSI tree to find the ScConstructorInvocation element.
   * If it is not present in the PSI tree an empty Optional is returned.
   */
  public Optional<PsiElement> traversePsiTree(Optional<PsiElement> templateDef) {
    if (templateDef.isPresent()) {
      PsiElement[] children = templateDef.get().getChildren();
      Optional<PsiElement> extendsBlock = Arrays.stream(children).filter(
          ScExtendsBlock.class::isInstance).findFirst();
      if (extendsBlock.isPresent()) {
        children = extendsBlock.get().getChildren();
        Optional<PsiElement> templateParents = Arrays.stream(children).filter(
            ScTemplateParents.class::isInstance).findFirst();
        if (templateParents.isPresent()) {
          children = templateParents.get().getChildren();
          return Arrays.stream(children).filter(
              ScConstructorInvocation.class::isInstance).findFirst();
          
        }
      }
    }
    return Optional.empty();
  }
  
  /**
   * Checks the name of the new variable.
   */
  public boolean checkName(Optional<PsiElement> element) {
    if (element.isPresent()) {
      ScPatternList patternList = (ScPatternList) element.get();
      Optional<PsiElement> refPattern = Arrays.stream(patternList.getChildren()).filter(
                ScReferencePattern.class::isInstance).findFirst();
      return refPattern.map(psiElement -> psiElement.getText().equals(variableName)).orElse(false);
    }
    return false;
  }
  
  /**
   * Checks the class of the new instance.
   */
  public boolean checkSimpleType(Optional<PsiElement>  element) {
    if (element.isPresent()) {
      ScSimpleTypeElement simpleTypeElement = (ScSimpleTypeElement) element.get();
      return simpleTypeElement.getText().equals(className);
    }
    return false;
  }
  
  /**
   * Checks the arguments provided to the constructor.
   */
  public boolean checkArgsList(Optional<PsiElement> element) {
    if (element.isPresent()) {
      ScArgumentExprList argElement = (ScArgumentExprList) element.get();
      PsiElement[] children = argElement.getChildren();
      List<String> fileArgsList = new ArrayList<>();
      for (PsiElement child: children) {
        fileArgsList.add(child.getText());
      }
      return Arrays.equals(argsList, fileArgsList.toArray());
    }
    return false;
  }

  public boolean checkNewObjectAssignment(Optional<PsiElement> simpleType, Optional<PsiElement> argList) {
    return this.checkSimpleType(simpleType)
                  && this.checkArgsList(argList);
  }
 
}
