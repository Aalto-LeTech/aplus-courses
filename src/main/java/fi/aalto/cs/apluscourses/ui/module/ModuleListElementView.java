package fi.aalto.cs.apluscourses.ui.module;

import fi.aalto.cs.apluscourses.ui.Binding;
import fi.aalto.cs.apluscourses.ui.base.ListElementView;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ModuleListElementView implements ListElementView {
  @Binding
  public JPanel basePanel;
  @Binding
  public JLabel nameLabel;
  @Binding
  public JLabel statusLabel;

  @Override
  public JComponent getRenderer() {
    return basePanel;
  }
}
