package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareToggleAction;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class AssistantModeAction extends DumbAwareToggleAction {
  private static final Logger logger = APlusLogger.logger;

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return PluginSettings.getInstance().isAssistantMode();
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean state) {
    PluginSettings.getInstance().setAssistantMode(state);
    logger.info("Assistant mode set to {}", state);
  }
}
