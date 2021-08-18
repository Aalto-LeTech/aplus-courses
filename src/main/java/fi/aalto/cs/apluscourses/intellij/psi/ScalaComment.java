package fi.aalto.cs.apluscourses.intellij.psi;

import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;

public class ScalaComment {
  
  private final String text;
  
  public ScalaComment(String text) {
    this.text = text;
  }
  
  public boolean checkText(ScalaPsiElement element) {
    return text.equals(element.getText());
  }
}
