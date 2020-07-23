package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import fi.aalto.cs.apluscourses.dal.APlusExerciseDataSource;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import org.junit.Test;

public class MainViewModelTest {

  @Test
  public void testSetExerciseDataSource() {
    Authentication authentication = mock(Authentication.class);

    MainViewModel mainViewModel = new MainViewModel();
    mainViewModel.setExerciseDataSource(APlusExerciseDataSource::new, () -> authentication);
    ExerciseDataSource exerciseDataSource = mainViewModel.getExerciseDataSource();

    assertNotNull(exerciseDataSource);
    assertSame(authentication, exerciseDataSource.getAuthentication());

    mainViewModel.clear();
  }
}
