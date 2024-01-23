package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.concurrency.ThreadContext;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.JBUI;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

public class IntegrityCheckDialog {

  private IntegrityCheckDialog() {

  }

  /**
   * Displays a dialog notifying the user that the plugin's required dependencies are missing.
   */
  public static void show() {
    var box = Box.createVerticalBox();

    LinkLabel<Object> linkLabel = new LinkLabel<>(getText("ui.integrityDialog.linkText"),
        AllIcons.Ide.External_link_arrow,
        (first, second) -> BrowserUtil.browse(getText("ui.integrityDialog.linkTarget")));
    linkLabel.setIconTextGap(0);
    linkLabel.setHorizontalTextPosition(SwingConstants.LEFT);

    box.add(new JBLabel(getText("ui.integrityDialog.description")));
    box.add(Box.createVerticalStrut(10));
    box.add(linkLabel);

    // This dialog box cannot be easily converted to Messages.showErrorDialog because the latter only accepts a string
    // as message text. Here we have a Swing component that includes a clickable link.
    // We'll go with the second-best solution, which is to temporarily reset the thread context manually.
    try (var ignored = ThreadContext.resetThreadContext()) {
      JOptionPane.showMessageDialog(null, JBUI.Panels.simplePanel(box), getText("ui.integrityDialog.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
