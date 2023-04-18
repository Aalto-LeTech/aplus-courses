package fi.aalto.cs.apluscourses.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.JBUI;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

public class IntegrityCheckDialog {

  public static void show() {
    var box = Box.createVerticalBox();

    JBLabel message = new JBLabel("<html>The A+ Courses plugin has detected that critical IntelliJ dependencies are missing. The plugin will not function without them.<br>This situation could have been caused by installing the plugin directly from disk, rather than using the JetBrains Marketplace.<br>Please reinstall the plugin from the Marketplace.");

    LinkLabel<Object> linkLabel = new LinkLabel<>("See detailed instructions at A+ Courses GitHub", AllIcons.Ide.External_link_arrow,
        (first, second) -> BrowserUtil.browse("https://github.com/Aalto-LeTech/aplus-courses/wiki/Installing-from-Marketplace"));
    linkLabel.setIconTextGap(0);
    linkLabel.setHorizontalTextPosition(SwingConstants.LEFT);

    box.add(message);
    box.add(Box.createVerticalStrut(10));
    box.add(linkLabel);

    JOptionPane.showMessageDialog(null, JBUI.Panels.simplePanel(box), "A+ Courses: Missing dependencies", JOptionPane.ERROR_MESSAGE);
  }

}
