package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
  public void testSortsExercises() {
    Exercise first = new Exercise(424, "Assignment 3",
        "http://localhost:1000/w10/ch02/w10_ch02_03/", 0, 0,0);
    Exercise second = new Exercise(325, "Feedback",
        "http://localhost:2000/w10/ch02/w10_ch02_feedback/", 0, 0, 0);
    Exercise third = new Exercise(195, "Assignment 1",
        "http://localhost:3000/w10/ch03/w10_ch03_01/", 0, 0, 0);
    Exercise fourth = new Exercise(908, "Feedback",
        "http://localhost:3000/w10/ch03/w10_ch03_feedback/", 0, 0, 0);
    Exercise fifth = new Exercise(282, "Assignment 1",
        "http://localhost:3000/w10/ch04/w10_ch04_01/", 0, 0, 0);

    ExerciseGroup group = new ExerciseGroup("", Arrays.asList(third, fifth, first, fourth, second));
    ExerciseGroupViewModel groupViewModel = new ExerciseGroupViewModel(group);
    List<ExerciseViewModel> exerciseViewModels = groupViewModel.getChildren().stream()
        .map(ExerciseViewModel.class::cast).collect(Collectors.toList());

    Long[] ids = exerciseViewModels
        .stream()
        .map(ExerciseViewModel::getModel)
        .map(Exercise::getId)
        .toArray(Long[]::new);

    Assert.assertArrayEquals("The exercises are sorted correctly",
        new Long[] {424L, 325L, 195L, 908L, 282L}, ids);
  }

}
