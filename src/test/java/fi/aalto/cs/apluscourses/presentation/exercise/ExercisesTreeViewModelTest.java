package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.Collections;
import org.junit.Test;

public class ExercisesTreeViewModelTest {
  @Test
  public void testIsEmptyTextVisible() {
    assertTrue(new EmptyExercisesTreeViewModel().isEmptyTextVisible());
    assertFalse(new ExercisesTreeViewModel(Collections.emptyList(), new Options())
        .isEmptyTextVisible());
  }
}
