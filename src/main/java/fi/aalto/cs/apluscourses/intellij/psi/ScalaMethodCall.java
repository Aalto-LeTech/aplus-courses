package fi.aalto.cs.apluscourses.intellij.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScArgumentExprList;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ScalaMethodCall {
  
  private final String methodName;
  private final String[] argsList;
  
  public ScalaMethodCall(String methodName, String[] argsList) {
    this.methodName = methodName;
    this.argsList = argsList;
  }
  
  public boolean checkMethodName(Optional<PsiElement> refExprMethodName) {
    if (refExprMethodName.isPresent()) {
      ScReferenceExpression refExpr = (ScReferenceExpression) refExprMethodName.get();
      return refExpr.getText().equals(methodName);
    }
    return false;
  }
  
  public boolean checkArguments(Optional<PsiElement> argsList) {
    if (argsList.isPresent()) {
      ScArgumentExprList refExpr = (ScArgumentExprList) argsList.get();
      PsiElement[] children = refExpr.getChildren();
      List<String> fileArgsList = new ArrayList<>();
      for (PsiElement child: children) {
        fileArgsList.add(child.getText());
      }
      return Arrays.equals(this.argsList, fileArgsList.toArray());
    }
    return false;
  }
}
