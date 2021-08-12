package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ResetSettingsAction extends DumbAwareAction {

  private static final Logger logger = APlusLogger.logger;

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    PluginSettings.getInstance().resetLocalSettings();
    logger.info("Reset local settings");
  }
}
