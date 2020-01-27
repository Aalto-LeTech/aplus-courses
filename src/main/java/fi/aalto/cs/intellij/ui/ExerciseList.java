package fi.aalto.cs.intellij.ui;

import fi.aalto.cs.intellij.annotations.Binding;
import javax.swing.JList;
import javax.swing.JPanel;

public class ExerciseList {

  @Binding
  private JList exercises;
  private JPanel basePanel;

  public JPanel getBasePanel() {
    return basePanel;
  }
}
