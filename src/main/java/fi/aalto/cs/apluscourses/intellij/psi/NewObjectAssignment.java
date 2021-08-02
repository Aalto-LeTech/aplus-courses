package fi.aalto.cs.apluscourses.intellij.psi;

import com.intellij.psi.PsiElement;

public class NewObjectAssignment {
  private final String variableName;

  public NewObjectAssignment(String variableName) {
    this.variableName = variableName;
  }

  public boolean checkVariableName(PsiElement element) {

    return false;
  }
}
