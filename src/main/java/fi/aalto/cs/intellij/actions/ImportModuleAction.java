package fi.aalto.cs.intellij.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import fi.aalto.cs.intellij.presentation.ModuleListModel;
import fi.aalto.cs.intellij.services.PluginSettings;
import java.awt.Component;
import jdk.internal.vm.compiler.collections.EconomicMap;
import org.jetbrains.annotations.NotNull;

public class ImportModuleAction extends AnAction {

  public static final String ACTION_ID = "fi.aalto.cs.intellij.actions.ImportModuleAction";

  @Override
  public void update(@NotNull AnActionEvent e) {
    ModuleListModel modules = PluginSettings.getInstance().getMainModel().getModules();
    boolean isModuleSelected = modules != null && !modules.getSelectionModel().isSelectionEmpty();
    e.getPresentation().setEnabled(isModuleSelected);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {

  }

  public static void trigger(Component source) {
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206130119/comments/206169635
    AnAction action = ActionManager.getInstance().getAction(ImportModuleAction.ACTION_ID);
    AnActionEvent event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN,
        DataManager.getInstance().getDataContext(source));
    action.actionPerformed(event);
  }
}
