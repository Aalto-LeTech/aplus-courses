package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.model.ExercisesTree;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesTreeViewModel extends BaseTreeViewModel<ExercisesTree>
    implements Searchable {
  private boolean isLoaded = false;

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public ExercisesTreeViewModel(@NotNull ExercisesTree exercisesTree,
                                @NotNull Options options) {
    this(exercisesTree, options, null);
  }


  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public ExercisesTreeViewModel(@NotNull ExercisesTree exercisesTree,
                                @NotNull Options options,
                                @Nullable CourseProject courseProject) {
    super(exercisesTree,
        exercisesTree.getExerciseGroups()
            .stream()
            .map(exerciseGroup -> new ExerciseGroupViewModel(exerciseGroup, courseProject))
            .collect(Collectors.toList()),
        options);
    setLoaded(courseProject != null && courseProject.getAuthentication() != null);
  }

  /**
   * Creates an EmptyExercisesTreeViewModel if the exercisesTree is null, else an ExercisesTreeViewModel.
   */
  @NotNull
  public static ExercisesTreeViewModel createExerciseTreeViewModel(@Nullable ExercisesTree exercisesTree,
                                                                   @NotNull Options options,
                                                                   @Nullable CourseProject courseProject) {
    if (exercisesTree == null) {
      return new EmptyExercisesTreeViewModel();
    }
    return new ExercisesTreeViewModel(exercisesTree, options, courseProject);
  }

  public String getName() {
    var student = getModel().getSelectedStudent();
    return student == null ? null : student.getFullName();
  }

  @NotNull
  public String getEmptyText() {
    return isLoaded ? getText("ui.exercise.ExercisesView.allAssignmentsFiltered") : getText("ui.toolWindow.loading");
  }

  @NotNull
  public String getTitleText() {
    return getName() == null ? getText("ui.toolWindow.subTab.exercises.name") :
        getAndReplaceText("ui.toolWindow.subTab.exercises.nameStudent", getName());
  }

  @Override
  @NotNull
  public Selection findSelected() {
    return new ExerciseTreeSelection(traverseAndFind(SelectableNodeViewModel::isSelected));
  }

  public boolean isLoaded() {
    return isLoaded;
  }

  public void setLoaded(boolean loaded) {
    isLoaded = loaded;
  }

  public static class ExerciseTreeSelection extends Selection {
    public ExerciseTreeSelection(@Nullable List<SelectableNodeViewModel<?>> pathToSelected) {
      super(pathToSelected);
    }

    @Nullable
    public ExerciseGroupViewModel getExerciseGroup() {
      var selection = getLevel(1);
      return selection instanceof ExerciseGroupViewModel ? (ExerciseGroupViewModel) selection : null;
    }

    @Nullable
    public ExerciseViewModel getExercise() {
      var selection = getLevel(2);
      return selection instanceof ExerciseViewModel ? (ExerciseViewModel) selection : null;
    }

    @Nullable
    public SubmissionResultViewModel getSubmissionResult() {
      var selection = getLevel(3);
      return selection instanceof SubmissionResultViewModel ? (SubmissionResultViewModel) selection : null;
    }
  }
}
