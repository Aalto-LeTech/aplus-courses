package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fi.aalto.cs.apluscourses.model.ExercisesTree;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import org.junit.jupiter.api.Test;

public class ExercisesTreeViewModelTest {
  @Test
  public void testIsEmptyTextVisible() {
    assertTrue(new EmptyExercisesTreeViewModel().isEmptyTextVisible());
    assertFalse(new ExercisesTreeViewModel(new ExercisesTree(), new Options())
        .isEmptyTextVisible());
  }
}
