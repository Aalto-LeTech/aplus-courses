package fi.aalto.cs.apluscourses.model;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void testUser() {
    var auth = mock(Authentication.class);
    var exerciseDataSource = new ModelExtensions.TestExerciseDataSource();
    var user = exerciseDataSource.getUser(auth);
    assertSame("test", user.getUserName());
    assertSame(auth, user.getAuthentication());
  }
}
