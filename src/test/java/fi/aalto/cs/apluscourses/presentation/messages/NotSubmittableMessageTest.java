package fi.aalto.cs.apluscourses.presentation.messages;

import static org.hamcrest.Matchers.containsString;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.messages.NotSubmittableMessage;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class NotSubmittableMessageTest {

  @Test
  public void testNotSubmittableMessage() {
    Exercise exercise = new Exercise(1, "ExOne", Collections.emptyList(), 0, 0, 0);
    NotSubmittableMessage message = new NotSubmittableMessage(exercise);
    Assert.assertThat("The content mentions the A+ web interface", message.getContent(),
        containsString("can only be submitted from the A+ web interface"));
    Assert.assertSame(exercise, message.getExercise());
  }

}
