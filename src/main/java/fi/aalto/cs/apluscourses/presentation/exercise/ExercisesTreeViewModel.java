package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesTreeViewModel implements TreeViewModel {

  private final List<ExerciseGroupViewModel> groupViewModels;

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public ExercisesTreeViewModel(@NotNull List<ExerciseGroup> exerciseGroups) {
    this.groupViewModels = exerciseGroups
        .stream()
        .map(ExerciseGroupViewModel::new)
        .collect(Collectors.toList());
  }

  /**
   * Construct an exercise tree view model from the given course view model and authentication view
   * model. Returns {@code null} if either of the given view models is {@code null} or the API
   * request fails in some way.
   */
  @Nullable
  public static ExercisesTreeViewModel fromCourseAndAuthentication(
      @Nullable CourseViewModel courseViewModel,
      @Nullable APlusAuthenticationViewModel authenticationViewModel) {
    if (courseViewModel == null || authenticationViewModel == null) {
      return null;
    }

    Course course = courseViewModel.getModel();
    APlusAuthentication authentication = authenticationViewModel.getAuthentication();
    try {
      return new ExercisesTreeViewModel(
          ExerciseGroup.getCourseExerciseGroups(course, authentication));
    } catch (InvalidAuthenticationException e) {
      // TODO: might want to communicate this to the user somehow
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Returns the exercise from this tree that is selected, or null if no exercise is selected.
   */
  @Nullable
  public ExerciseViewModel getSelectedExercise() {
    for (ExerciseGroupViewModel groupViewModel : groupViewModels) {
      Optional<ExerciseViewModel> selected = groupViewModel
          .getExerciseViewModels()
          .stream()
          .filter(ExerciseViewModel::isSelected)
          .findFirst();
      if (selected.isPresent()) {
        return selected.get();
      }
    }
    return null;
  }

  /**
   * Construct a {@link TreeModel} instance out of this view model.
   */
  @NotNull
  @Override
  public TreeModel toTreeModel() {
    if (groupViewModels.isEmpty()) {
      return new DefaultTreeModel(null);
    }

    DefaultMutableTreeNode root = new DefaultMutableTreeNode(null);
    groupViewModels.forEach(groupViewModel -> {
      DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(groupViewModel);
      groupViewModel
          .getExerciseViewModels()
          .forEach(exerciseViewModel ->
              groupNode.add(new DefaultMutableTreeNode(exerciseViewModel, false)));
      root.add(groupNode);
    });
    return new DefaultTreeModel(root);
  }

}
