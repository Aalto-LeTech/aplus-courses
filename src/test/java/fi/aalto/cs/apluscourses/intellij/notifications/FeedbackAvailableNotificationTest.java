package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import com.intellij.notification.Notification;
import fi.aalto.cs.apluscourses.intellij.actions.OpenSubmissionNotificationAction;
import fi.aalto.cs.apluscourses.intellij.actions.ShowFeedbackNotificationAction;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import java.util.Collections;
import java.util.OptionalLong;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeedbackAvailableNotificationTest {

  @Test
  void testFeedbackAvailableNotificationTest() {
    var info = new SubmissionInfo(Collections.emptyMap());
    var exercise = new Exercise(123, "Test Exercise", "https://example.com", info, 5, 10, OptionalLong.empty(), null);
    SubmissionResult result
        = new SubmissionResult(0, 0, 0.0, SubmissionResult.Status.GRADED, exercise);
    Notification notification = new FeedbackAvailableNotification(result, exercise);

    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    MatcherAssert.assertThat("The content contains the exercise name", notification.getContent(),
        containsString("Test Exercise"));
    Assertions.assertTrue(notification.getActions().get(0) instanceof ShowFeedbackNotificationAction,
        "The notification has the correct action");
    Assertions.assertTrue(notification.getActions().get(1) instanceof OpenSubmissionNotificationAction,
        "The notification has the correct action");
  }

}
