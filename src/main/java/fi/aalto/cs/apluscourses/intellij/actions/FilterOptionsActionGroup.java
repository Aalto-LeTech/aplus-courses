package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.utils.Streamable;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterOptionsActionGroup extends ActionGroup {

  private final MainViewModelProvider mainViewModelProvider;

  public FilterOptionsActionGroup() {
    this(PluginSettings.getInstance());
  }

  public FilterOptionsActionGroup(MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  // Caution: Array covariance in the return type.
  @Override
  public @NotNull FilterOptionAction[] getChildren(@Nullable AnActionEvent e) {
    return Optional.ofNullable(e)
        .map(AnActionEvent::getProject)
        .map(mainViewModelProvider::getMainViewModel)
        .map(MainViewModel::getExercises)
        .map(BaseTreeViewModel::getFilterOptions)
        .map(Streamable::stream)
        .orElseGet(Stream::empty)
        .map(FilterOptionAction::new)
        .toArray(FilterOptionAction[]::new);
  }
}
