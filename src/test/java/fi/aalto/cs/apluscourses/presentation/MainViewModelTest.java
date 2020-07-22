package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import fi.aalto.cs.apluscourses.dal.APlusExerciseDataSource;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import org.junit.Test;

public class MainViewModelTest {

  @Test
  public void testSetExerciseDataSourceAndClear() {
    Authentication authetication = mock(Authentication.class);

    MainViewModel mainViewModel = new MainViewModel();
    mainViewModel.setExerciseDataSource(APlusExerciseDataSource::new, () -> authetication);
    ExerciseDataSource exerciseDataSource = mainViewModel.getExerciseDataSource();

    assertNotNull(exerciseDataSource);
    assertSame(authetication, exerciseDataSource.getAuthentication());

    verify(authetication, never()).clear();

    mainViewModel.clear();

    verify(authetication).clear();
  }
}
