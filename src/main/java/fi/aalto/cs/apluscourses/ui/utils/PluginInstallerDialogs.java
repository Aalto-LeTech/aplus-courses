package fi.aalto.cs.apluscourses.ui.utils;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.utils.PluginDependency;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class PluginInstallerDialogs {

  private static @NotNull String pluginListToString(@NotNull List<PluginDependency> pluginNames) {
    return pluginNames.stream().map(x -> " - " + x.getDisplayName()).collect(Collectors.joining("<br>"));
  }

  public static PluginInstallerCallback.ConsentResult askForInstallationConsentOnCreation(@NotNull List<PluginDependency> pluginNames) {
    Object[] options = {
        "Install",
        "Ignore",
        "Cancel"
    };

    final int result = JOptionPane.showOptionDialog(null, "<html>This course requires the following additional IntelliJ plugins:<br>" + pluginListToString(pluginNames) + "</html>",
        "Additional dependencies", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

    switch (result) {
      case 0:
        return PluginInstallerCallback.ConsentResult.ACCEPTED;
      case 1:
        return PluginInstallerCallback.ConsentResult.IGNORE_INSTALL;
      default:
        return PluginInstallerCallback.ConsentResult.REJECTED;
    }
  }

  public static boolean askForIDERestart() {
    return Messages.showOkCancelDialog(
        getText("ui.pluginInstallationDialog.askForIDERestart.message"),
        getText("ui.pluginInstallationDialog.askForIDERestart.title"),
        getText("ui.pluginInstallationDialog.askForIDERestart.okText"),
        getText("ui.pluginInstallationDialog.askForIDERestart.cancelText"),
        Messages.getQuestionIcon()) == Messages.OK;
  }

  private PluginInstallerDialogs() {

  }
}
