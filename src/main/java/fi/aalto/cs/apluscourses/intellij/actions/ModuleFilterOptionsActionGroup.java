package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.presentation.module.ModuleListViewModel;
import fi.aalto.cs.apluscourses.utils.Streamable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleFilterOptionsActionGroup extends DefaultActionGroup implements DumbAware, Toggleable {

  private final MainViewModelProvider mainViewModelProvider;

  private ModuleFilterOptionAction @NotNull [] filterOptionActions = new ModuleFilterOptionAction[0];

  public ModuleFilterOptionsActionGroup() {
    this(PluginSettings.getInstance());
  }

  public ModuleFilterOptionsActionGroup(MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  @Override
  public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
    this.filterOptionActions = getFilterOptionActions(e);
    List<AnAction> actions = new ArrayList<>(Arrays.asList(this.filterOptionActions));
    if (e != null) {
      actions.add(new Separator());
      Options filterOptions =
          mainViewModelProvider.getMainViewModel(e.getProject()).getModuleFilterOptions();
      actions.add(new SelectAllFiltersAction(filterOptions));
    }
    return actions.toArray(new AnAction[0]);
  }

  /**
   * Returns filter option actions.
   *
   * @param e AnActionEvent
   * @return An array of FilterOptionActions
   */
  private ModuleFilterOptionAction @NotNull [] getFilterOptionActions(@Nullable AnActionEvent e) {
    return Optional.ofNullable(e)
        .map(AnActionEvent::getProject)
        .map(mainViewModelProvider::getMainViewModel)
        .map(MainViewModel::getModules)
        .map(ModuleListViewModel::getFilterOptions)
        .map(Streamable::stream)
        .orElseGet(Stream::empty)
        .map(ModuleFilterOptionAction::new)
        .toArray(ModuleFilterOptionAction[]::new);
  }

  public ModuleFilterOptionAction @NotNull [] getFilterOptionActions() {
    return filterOptionActions;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    var modulesListViewModel =
        mainViewModelProvider.getMainViewModel(project).courseViewModel.get().getModules();
    e.getPresentation().setEnabled(modulesListViewModel != null);
    boolean filterActive =
        Arrays.stream(this.filterOptionActions).anyMatch(action -> !action.isSelected(e));
    Toggleable.setSelected(e.getPresentation(), filterActive);
  }
}
