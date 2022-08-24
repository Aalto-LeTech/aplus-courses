package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class APlusOptionsActionGroup extends OptionsActionGroup {

  protected @NotNull MainViewModel getMainViewModel(@Nullable Project project) {
    return PluginSettings.getInstance().getMainViewModel(project);
  }

  public static class ForModules extends APlusOptionsActionGroup {
    @Override
    public @Nullable Options getOptions(@Nullable Project project) {
      return getMainViewModel(project).getModuleOptions();
    }

    @Override
    public boolean isAvailable(@Nullable Project project) {
      return getMainViewModel(project).courseViewModel.get() != null;
    }
  }

  public static class ForExercises extends APlusOptionsActionGroup {
    @Override
    public boolean isAvailable(@Nullable Project project) {
      return getMainViewModel(project).exercisesViewModel.get() != null;
    }

    @Override
    public @Nullable Options getOptions(@Nullable Project project) {
      return getMainViewModel(project).getExerciseOptions();
    }
  }
}
