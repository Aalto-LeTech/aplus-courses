package fi.aalto.cs.apluscourses.ui.module;

import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.ListElementView;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ModuleListElementView implements ListElementView {
  @GuiObject
  public JPanel basePanel;
  @GuiObject
  public JLabel nameLabel;
  @GuiObject
  public JLabel statusLabel;

  @Override
  public JComponent getRenderer() {
    return basePanel;
  }
}
