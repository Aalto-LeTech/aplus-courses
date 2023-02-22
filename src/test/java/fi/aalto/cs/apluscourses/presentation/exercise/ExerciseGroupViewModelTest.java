package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.mockito.Mockito.mock;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.Returns;

class ExerciseGroupViewModelTest {

  @Test
  void testGetPresentableName() {
    var group1 = new ExerciseGroup(1, "|fi:Ryhma|en:Group|", "", true, List.of(), List.of());
    ExerciseGroupViewModel viewModel1 = new ExerciseGroupViewModel(group1);

    var group2 = new ExerciseGroup(2, "group name", "", true, List.of(), List.of());
    ExerciseGroupViewModel viewModel2 = new ExerciseGroupViewModel(group2);

    Assertions.assertEquals("Group", viewModel1.getPresentableName(), "getPresentableName returns the English name");
    Assertions.assertEquals("group name", viewModel2.getPresentableName(), "getPresentableName returns correct name");
  }

  @Test
  void testSortsExercises1() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise first = new Exercise(424, "Assignment 3",
        "http://localhost:1000/w10/ch02/w10_ch02_03/", info, 0, 0, OptionalLong.empty(), null, false);
    Exercise second = new Exercise(325, "Feedback",
        "http://localhost:1000/w10/ch02/w10_ch02_feedback/", info, 0, 0, OptionalLong.empty(), null, false);
    Exercise third = new Exercise(195, "Assignment 9",
        "http://localhost:1000/w10/ch03/w10_ch03_9/", info, 0, 0, OptionalLong.empty(), null, false);
    Exercise fourth = new Exercise(282, "Assignment 10",
        "http://localhost:1000/w10/ch04/w10_ch03_10/", info, 0, 0, OptionalLong.empty(), null, false);
    Exercise fifth = new Exercise(908, "Assignment 1",
        "http://localhost:1000/w12/ch01/w12_ch01_1/", info, 0, 0, OptionalLong.empty(), null, false);


    ExerciseGroup group = new ExerciseGroup(5, "", "", true, List.of(),
        List.of(424L, 325L, 195L, 282L, 908L));
    List.of(third, fifth, first, fourth, second).forEach(group::addExercise);
    ExerciseGroupViewModel groupViewModel = new ExerciseGroupViewModel(group);
    List<ExerciseViewModel> exerciseViewModels = groupViewModel.getChildren().stream()
        .map(ExerciseViewModel.class::cast).collect(Collectors.toList());

    Long[] ids = exerciseViewModels
        .stream()
        .map(ExerciseViewModel::getModel)
        .map(Exercise::getId)
        .toArray(Long[]::new);

    Assertions.assertArrayEquals(new Long[] {424L, 325L, 195L, 282L, 908L}, ids, "The exercises are sorted correctly");
  }

  @Test
  void testSortsExercises2() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise first = new Exercise(424, "Assignment 3",
        "http://localhost:1000/studio_2/k2021dev/k15A/osa01/k15A_osa01_1/", info, 0, 0,
        OptionalLong.empty(), null, false);
    Exercise second = new Exercise(325, "Feedback",
        "http://localhost:1000/studio_2/k2021dev/k15A/osa01/k15A_osa01_10/", info, 0, 0,
        OptionalLong.empty(), null, false);
    Exercise third = new Exercise(195, "Assignment 9",
        "http://localhost:1000/studio_2/k2021dev/k15A/osa01/k15B_osa01_1/", info, 0, 0,
        OptionalLong.empty(), null, false);


    ExerciseGroup group = new ExerciseGroup(5, "", "", true, List.of(),
        List.of(424L, 325L, 195L));
    List.of(third, second, first).forEach(group::addExercise);
    ExerciseGroupViewModel groupViewModel = new ExerciseGroupViewModel(group);
    List<ExerciseViewModel> exerciseViewModels = groupViewModel.getChildren().stream()
        .map(ExerciseViewModel.class::cast).collect(Collectors.toList());

    Long[] ids = exerciseViewModels
        .stream()
        .map(ExerciseViewModel::getModel)
        .map(Exercise::getId)
        .toArray(Long[]::new);

    Assertions.assertArrayEquals(new Long[] {424L, 325L, 195L}, ids, "The exercises are sorted correctly");
  }

  @Test
  void testFilterVisibility() throws InterruptedException {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise = new Exercise(424, "Assignment 3",
        "http://localhost:1000/studio_2/k2021dev/k15A/osa01/k15A_osa01_1/", info, 0, 0,
        OptionalLong.empty(), null, false);

    ExerciseGroup group = new ExerciseGroup(5, "", "", true, List.of(), List.of());
    group.addExercise(exercise);
    ExerciseGroupViewModel groupViewModel = new ExerciseGroupViewModel(group);

    Filter filter = mock(Filter.class, new Returns(Optional.empty()));
    groupViewModel.applyFilterRecursive(filter);
    Assertions.assertTrue(groupViewModel.isVisible(), "Week with children is visible");

    ExerciseGroup emptyGroup = new ExerciseGroup(6, "", "", true, List.of(), List.of());
    ExerciseGroupViewModel emptyGroupViewModel = new ExerciseGroupViewModel(emptyGroup);

    emptyGroupViewModel.applyFilterRecursive(filter);
    Assertions.assertFalse(emptyGroupViewModel.isVisible(), "Week with no children is not visible");
  }

}
