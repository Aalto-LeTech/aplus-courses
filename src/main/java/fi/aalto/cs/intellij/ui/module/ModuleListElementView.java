package fi.aalto.cs.intellij.ui.module;

import fi.aalto.cs.intellij.annotations.Binding;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ModuleListElementView {
  @Binding
  public JPanel basePanel;
  @Binding
  public JLabel nameLabel;
  @Binding
  public JLabel statusLabel;
}
