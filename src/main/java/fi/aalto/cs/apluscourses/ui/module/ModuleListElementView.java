package fi.aalto.cs.apluscourses.ui.module;

import fi.aalto.cs.apluscourses.ui.Binding;
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
