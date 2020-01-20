package fi.aalto.cs.intellij.ui;

import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import fi.aalto.cs.intellij.services.CourseInformation;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;

public class ModuleList {
  private JBList<String> modules;
  private JPanel basePanel;

  /**
   * Initializes a panel with a list of modules.
   */
  public ModuleList() {
    basePanel = new JPanel();
    basePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    modules = new JBList<String>();
    CourseInformation courseInformation = CourseInformation.getInstance();
    List<String> moduleNames = courseInformation.getModuleNames();
    DefaultListModel<String> listModel = new DefaultListModel<String>();
    for (String moduleName : moduleNames) {
      listModel.addElement(moduleName);
    }
    modules.setModel(listModel);
    // GUI designer
    basePanel.add(modules,
        new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null,
            new Dimension(150, 50), null, 0, false));
  }

  public JPanel getBasePanel() {
    return basePanel;
  }
}
