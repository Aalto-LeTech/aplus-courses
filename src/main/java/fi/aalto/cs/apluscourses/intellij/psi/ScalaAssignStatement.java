package fi.aalto.cs.apluscourses.intellij.psi;

import com.intellij.psi.PsiElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ScalaAssignStatement {
  
  private final String variableName;
  private final String[] valueTokens;
  
  public ScalaAssignStatement(String variableName, String[] valueTokens) {
    this.variableName = variableName;
    this.valueTokens = valueTokens;
  }
  
  public boolean checkEquals(PsiElement equals) {
    return equals != null && ("=").equals(equals.getText());
  }
  
  /**
   * Checks the assigned expression.
   */
  public boolean checkInfixExpr(Optional<PsiElement> infixExpr) {
    if (infixExpr.isPresent()) {
      PsiElement[] children = infixExpr.get().getChildren();
      if (children.length > 0) {
        Collection<PsiElement> tokens = PsiUtil.getPsiElementsSiblings(children[0]);
        List<String> fileTokens = new ArrayList<>();
        tokens.forEach(t -> fileTokens.add(t.getText()));
        return Arrays.equals(valueTokens, fileTokens.toArray());
      }
    }
    return false;
  }
  
  /**
   * Checks the name of the variable.
   */
  public boolean checkVariableName(Optional<PsiElement> refExpr) {
    if (refExpr.isPresent()) {
      return variableName.equals(refExpr.get().getText());
    }
    return false;
  }
}
