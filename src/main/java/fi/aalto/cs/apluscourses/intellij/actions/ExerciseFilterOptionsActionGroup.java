package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.utils.Streamable;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExerciseFilterOptionsActionGroup extends ActionGroup {

  private final MainViewModelProvider mainViewModelProvider;

  public ExerciseFilterOptionsActionGroup(MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  public ExerciseFilterOptionsActionGroup() {
    this(PluginSettings.getInstance());
  }

  @Override
  public @NotNull AnAction[] getChildren(@Nullable AnActionEvent e) {
    return Optional.ofNullable(e)
        .map(AnActionEvent::getProject)
        .map(mainViewModelProvider::getMainViewModel)
        .map(MainViewModel::getExercises)
        .map(BaseTreeViewModel::getFilterOptions)
        .map(Streamable::stream)
        .orElseGet(Stream::empty)
        .map(FilterOptionAction::new)
        .toArray(AnAction[]::new);
  }
}
