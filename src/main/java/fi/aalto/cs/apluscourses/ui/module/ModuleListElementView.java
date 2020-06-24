package fi.aalto.cs.apluscourses.ui.module;

import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.ListElementView;
import icons.PluginIcons;
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
  @GuiObject
  public JLabel updateLabel;

  public ModuleListElementView() {
    nameLabel.setIcon(PluginIcons.A_PLUS_MODULE);
  }

  @Override
  public JComponent getRenderer() {
    return basePanel;
  }
}
