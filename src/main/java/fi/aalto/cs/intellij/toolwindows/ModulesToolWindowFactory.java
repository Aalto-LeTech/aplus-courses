package fi.aalto.cs.intellij.toolwindows;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.intellij.actions.ActionGroups;
import fi.aalto.cs.intellij.services.PluginSettings;
import fi.aalto.cs.intellij.ui.module.ModuleListView;
import fi.aalto.cs.intellij.ui.module.ModulesView;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ModulesToolWindowFactory extends BaseToolWindowFactory {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ModulesView modulesView = new ModulesView();
    ModuleListView moduleListView = modulesView.getModuleListView();
    modulesView.setModel(PluginSettings.getInstance().getMainModel());

    ActionManager actionManager = ActionManager.getInstance();
    ActionGroup group = (ActionGroup) actionManager.getAction(ActionGroups.MODULE_ACTIONS);

    ActionToolbar toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    toolbar.setTargetComponent(moduleListView);
    modulesView.getToolbarContainer().add(toolbar.getComponent());

    ActionPopupMenu popupMenu =
        actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, group);
    popupMenu.setTargetComponent(moduleListView);
    moduleListView.setPopupMenu(popupMenu.getComponent());

    return modulesView.getBasePanel();
  }
}
