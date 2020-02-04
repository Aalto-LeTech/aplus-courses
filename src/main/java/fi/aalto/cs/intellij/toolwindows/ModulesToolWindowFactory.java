package fi.aalto.cs.intellij.toolwindows;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.intellij.actions.ActionGroups;
import fi.aalto.cs.intellij.actions.ActionUtil;
import fi.aalto.cs.intellij.actions.ImportModuleAction;
import fi.aalto.cs.intellij.services.PluginSettings;
import fi.aalto.cs.intellij.ui.module.ModulesView;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ModulesToolWindowFactory extends BaseToolWindowFactory implements DumbAware {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ModulesView modulesView = new ModulesView();
    PluginSettings.getInstance().getMainModel().course.addValueChangedObserver(modulesView);

    ActionManager actionManager = ActionManager.getInstance();
    ActionGroup group = (ActionGroup) actionManager.getAction(ActionGroups.MODULE_ACTIONS);

    ActionToolbar toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    toolbar.setTargetComponent(modulesView.moduleListView);
    modulesView.toolbarContainer.add(toolbar.getComponent());

    ActionPopupMenu popupMenu =
        actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, group);
    popupMenu.setTargetComponent(modulesView.moduleListView);
    modulesView.moduleListView.setPopupMenu(popupMenu.getComponent());

    modulesView.moduleListView.addListActionListener(ActionUtil.triggerer(
        ImportModuleAction.ACTION_ID, modulesView.moduleListView));

    return modulesView.basePanel;
  }
}
