package fi.aalto.cs.apluscourses.ui.utils;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.utils.PluginDependency;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class PluginInstallerDialogs {

  private static @NotNull String pluginListToString(@NotNull List<PluginDependency> pluginNames) {
    return pluginNames.stream().map(x -> " - " + x.getDisplayName()).collect(Collectors.joining("<br>"));
  }

  /**
   * Shows a dialog asking the user to install required plugins. The dialog is supposed to be shown
   * during linking a project to an A+ course.
   */
  public static PluginInstallerCallback.ConsentResult askForInstallationConsentOnCreation(
      @NotNull List<PluginDependency> pluginNames) {
    String[] options = {
        getText("ui.pluginInstallationDialog.courseCreateDialog.yesText"),
        getText("ui.pluginInstallationDialog.courseCreateDialog.noText"),
        getText("ui.pluginInstallationDialog.courseCreateDialog.cancelText")
    };

    final int result = Messages.showDialog(
        getAndReplaceText("ui.pluginInstallationDialog.courseCreateDialog.message", pluginListToString(pluginNames)),
        getText("ui.pluginInstallationDialog.courseCreateDialog.title"),
        options,
        0,
        null);

    return switch (result) {
      case 0 -> PluginInstallerCallback.ConsentResult.ACCEPTED;
      case 1 -> PluginInstallerCallback.ConsentResult.IGNORE_INSTALL;
      default -> PluginInstallerCallback.ConsentResult.REJECTED;
    };
  }

  /**
   * Shows a dialog asking the user to install required plugins. The dialog is supposed to be shown
   * when opening an A+ project.
   */
  public static PluginInstallerCallback.ConsentResult askForInstallationConsentOnInit(
      @NotNull List<PluginDependency> pluginNames) {
    String[] options = {
        getText("ui.pluginInstallationDialog.courseOpenDialog.yesText"),
        getText("ui.pluginInstallationDialog.courseOpenDialog.noText")
    };

    final int result = Messages.showDialog(
        getAndReplaceText("ui.pluginInstallationDialog.courseOpenDialog.message", pluginListToString(pluginNames)),
        getText("ui.pluginInstallationDialog.courseOpenDialog.title"),
        options,
        0,
        null);

    return result == 0 ? PluginInstallerCallback.ConsentResult.ACCEPTED
        : PluginInstallerCallback.ConsentResult.IGNORE_INSTALL;
  }

  /**
   * Shows a dialog asking the user to restart the IDE. The dialog is supposed to be shown
   * when new plugins have been automatically installed or enabled, and an IDE restart is required.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
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
