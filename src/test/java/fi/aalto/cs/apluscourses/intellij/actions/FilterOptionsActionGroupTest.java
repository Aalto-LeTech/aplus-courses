package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FilterOptionsActionGroupTest {

  @Test
  public void testGetChildren() {
    Project project = mock(Project.class);
    AnActionEvent e = mock(AnActionEvent.class);
    when(e.getProject()).thenReturn(project);

    Option filterOption1 = new Option("Just some filter", null, item -> Optional.of(true)).init();
    Option filterOption2 = new Option("Another filter", null, item -> Optional.of(false)).init();
    Options filterOptions = new Options(filterOption1, filterOption2);

    MainViewModel mainViewModel = new MainViewModel(new Options());
    ExercisesTreeViewModel exercisesViewModel =
        spy(new ExercisesTreeViewModel(Collections.emptyList(), new Options()));
    when(exercisesViewModel.getFilterOptions()).thenReturn(filterOptions);
    mainViewModel.exercisesViewModel.set(exercisesViewModel);
    MainViewModelProvider mainViewModelProvider = mock(MainViewModelProvider.class);
    when(mainViewModelProvider.getMainViewModel(any())).thenReturn(mainViewModel);

    FilterOptionsActionGroup actionGroup = new FilterOptionsActionGroup(mainViewModelProvider);
    FilterOptionAction[] children = actionGroup.getChildren(e);

    assertSame(filterOption1, children[0].getOption());
    assertSame(filterOption2, children[1].getOption());
  }
}
