package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;

public class ExerciseGroupViewModelTest {

  @Test
  public void testGetPresentableName() {
    ExerciseGroup group1 = new ExerciseGroup(
        1, "|fi:Ryhma|en:Group|", "", true, Collections.emptyList());
    ExerciseGroupViewModel viewModel1 = new ExerciseGroupViewModel(group1);

    ExerciseGroup group2 = new ExerciseGroup(2, "group name", "", true, Collections.emptyList());
    ExerciseGroupViewModel viewModel2 = new ExerciseGroupViewModel(group2);

    Assert.assertEquals("getPresentableName returns the English name",
        "Group", viewModel1.getPresentableName());
    Assert.assertEquals("getPresentableName returns correct name",
        "group name", viewModel2.getPresentableName());
  }

  @Test
  public void testSortsExercises1() {
    Exercise first = new Exercise(424, "Assignment 3",
        "http://localhost:1000/w10/ch02/w10_ch02_03/", 0, 0,0, true);
    Exercise second = new Exercise(325, "Feedback",
        "http://localhost:1000/w10/ch02/w10_ch02_feedback/", 0, 0, 0, true);
    Exercise third = new Exercise(195, "Assignment 9",
        "http://localhost:1000/w10/ch03/w10_ch03_9/", 0, 0, 0, false);
    Exercise fourth = new Exercise(282, "Assignment 10",
        "http://localhost:1000/w10/ch04/w10_ch03_10/", 0, 0, 0, false);
    Exercise fifth = new Exercise(908, "Assignment 1",
        "http://localhost:1000/w12/ch01/w12_ch01_1/", 0, 0, 0, true);


    ExerciseGroup group =
        new ExerciseGroup(5, "", "", true, List.of(third, fifth, first, fourth, second));
    ExerciseGroupViewModel groupViewModel = new ExerciseGroupViewModel(group);
    List<ExerciseViewModel> exerciseViewModels = groupViewModel.getChildren().stream()
        .map(ExerciseViewModel.class::cast).collect(Collectors.toList());

    Long[] ids = exerciseViewModels
        .stream()
        .map(ExerciseViewModel::getModel)
        .map(Exercise::getId)
        .toArray(Long[]::new);

    Assert.assertArrayEquals("The exercises are sorted correctly",
        new Long[] {424L, 325L, 195L, 282L, 908L}, ids);
  }

  @Test
  public void testSortsExercises2() {
    Exercise first = new Exercise(424, "Assignment 3",
        "http://localhost:1000/studio_2/k2021dev/k15A/osa01/k15A_osa01_1/", 0, 0,0, false);
    Exercise second = new Exercise(325, "Feedback",
        "http://localhost:1000/studio_2/k2021dev/k15A/osa01/k15A_osa01_10/", 0, 0, 0, true);
    Exercise third = new Exercise(195, "Assignment 9",
        "http://localhost:1000/studio_2/k2021dev/k15A/osa01/k15B_osa01_1/", 0, 0, 0, true);


    ExerciseGroup group =
        new ExerciseGroup(5, "", "", true, List.of(third, second, first));
    ExerciseGroupViewModel groupViewModel = new ExerciseGroupViewModel(group);
    List<ExerciseViewModel> exerciseViewModels = groupViewModel.getChildren().stream()
        .map(ExerciseViewModel.class::cast).collect(Collectors.toList());

    Long[] ids = exerciseViewModels
        .stream()
        .map(ExerciseViewModel::getModel)
        .map(Exercise::getId)
        .toArray(Long[]::new);

    Assert.assertArrayEquals("The exercises are sorted correctly",
        new Long[] {424L, 325L, 195L}, ids);
  }

  @Test
  public void testFilterVisibility() throws InterruptedException {
    Exercise exercise = new Exercise(424, "Assignment 3",
        "http://localhost:1000/studio_2/k2021dev/k15A/osa01/k15A_osa01_1/", 0, 0, 0, false);

    ExerciseGroup group =
        new ExerciseGroup(5, "", "", true, Collections.singletonList(exercise));
    ExerciseGroupViewModel groupViewModel = new ExerciseGroupViewModel(group);

    Filter filter = mock(Filter.class, new Returns(Optional.empty()));
    groupViewModel.applyFilter(filter);
    assertTrue("Week with children is visible", groupViewModel.isVisible());

    ExerciseGroup emptyGroup =
        new ExerciseGroup(6, "", "", true, Collections.emptyList());
    ExerciseGroupViewModel emptyGroupViewModel = new ExerciseGroupViewModel(emptyGroup);

    emptyGroupViewModel.applyFilter(filter);
    assertFalse("Week with no children is not visible", emptyGroupViewModel.isVisible());
  }

}
