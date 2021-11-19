package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.ExercisesTree;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import org.jetbrains.annotations.NotNull;

public class OpenExerciseItemAction extends OpenItemAction<ExercisesTree> {

  public static final String ACTION_ID = OpenExerciseItemAction.class.getCanonicalName();

  /**
   * Construct an {@link OpenExerciseItemAction} instance with the given parameters. This constructor
   * is mainly useful for testing purposes.
   */
  public OpenExerciseItemAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                @NotNull UrlRenderer urlRenderer,
                                @NotNull Notifier notifier) {
    super(mainViewModelProvider, urlRenderer, notifier);
  }

  /**
   * Construct an {@link OpenExerciseItemAction} instance with reasonable defaults.
   */
  public OpenExerciseItemAction() {
    super();
  }

  @Override
  BaseTreeViewModel<ExercisesTree> getTreeViewModel(@NotNull Project project) {
    return mainViewModelProvider.getMainViewModel(project).exercisesViewModel.get();
  }
}
