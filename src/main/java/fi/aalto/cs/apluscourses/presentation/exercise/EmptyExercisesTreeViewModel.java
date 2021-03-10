package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.presentation.filter.Options;

import java.util.ArrayList;


public class EmptyExercisesTreeViewModel extends ExercisesTreeViewModel {

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public EmptyExercisesTreeViewModel() {
    super(new ArrayList<>(), new Options());
  }

  @Override
  public boolean isEmptyTextVisible() {
    return true;
  }
}
