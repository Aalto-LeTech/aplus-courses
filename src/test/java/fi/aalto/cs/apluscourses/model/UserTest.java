package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void testUser() {
    var auth = mock(Authentication.class);
    var exerciseDataSource = new ModelExtensions.TestExerciseDataSource();
    var user = exerciseDataSource.getUser(auth);
    Assertions.assertSame("test", user.getUserName());
    Assertions.assertSame(auth, user.getAuthentication());
  }
}
