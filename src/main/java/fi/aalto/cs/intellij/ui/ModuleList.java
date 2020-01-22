package fi.aalto.cs.intellij.ui;

import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import fi.aalto.cs.intellij.services.CourseInformation;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleList {
  private static final Logger logger = LoggerFactory
      .getLogger(CourseInformation.class);

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

    modules.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JBList list = (JBList) e.getSource();
        if (e.getClickCount() == 2) {
          int index = list.getSelectedIndex();
          String moduleName = (String) list.getModel().getElementAt(index);
          try {
            Desktop.getDesktop().browse(courseInformation.getModuleUrl(moduleName).toURI());
          } catch (URISyntaxException | IOException ex) {
            logger.error("Failed to open module url in browser");
          }
        }
      }
    });

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
