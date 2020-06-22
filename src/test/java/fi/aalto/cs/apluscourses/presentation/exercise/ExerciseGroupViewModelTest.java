package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseGroupViewModelTest {

  @Test
  public void testGetPresentableName() {
    ExerciseGroup group1 = new ExerciseGroup("|fi:Ryhma|en:Group|", Collections.emptyList());
    ExerciseGroupViewModel viewModel1 = new ExerciseGroupViewModel(group1);

    ExerciseGroup group2 = new ExerciseGroup("group name", Collections.emptyList());
    ExerciseGroupViewModel viewModel2 = new ExerciseGroupViewModel(group2);

    Assert.assertEquals("getPresentableName returns the English name",
        "Group", viewModel1.getPresentableName());
    Assert.assertEquals("getPresentableName returns correct name",
        "group name", viewModel2.getPresentableName());
  }

  @Test
  public void testRemovesFeedbackExercises() {
    Exercise nonFeedback = new Exercise(0, "Cool Exercise");
    Exercise feedback = new Exercise(1, "Feedback");

    ExerciseGroup group = new ExerciseGroup("group", Arrays.asList(nonFeedback, feedback));
    ExerciseGroupViewModel viewModel = new ExerciseGroupViewModel(group);

    Assert.assertEquals("The feedback exercise is removed",
        1, viewModel.getExerciseViewModels().size());
    Assert.assertEquals("The feedback exercise is removed",
        "Cool Exercise", viewModel.getExerciseViewModels().get(0).getPresentableName());
  }

  @Test
  public void testSortsExercises() {
    Exercise first = new Exercise(1, "No assignment number");
    Exercise second = new Exercise(2, "Assignment 5");
    Exercise third = new Exercise(3, "Assignment 10");

    ExerciseGroup group = new ExerciseGroup("", Arrays.asList(third, second, first));
    ExerciseGroupViewModel groupViewModel = new ExerciseGroupViewModel(group);
    List<ExerciseViewModel> exerciseViewModels = groupViewModel.getExerciseViewModels();

    String message = "The exercises are sorted by assignment numbers";

    Assert.assertEquals(message, first.getName(), exerciseViewModels.get(0).getPresentableName());
    Assert.assertEquals(message, second.getName(), exerciseViewModels.get(1).getPresentableName());
    Assert.assertEquals(message, third.getName(), exerciseViewModels.get(2).getPresentableName());
  }

}
