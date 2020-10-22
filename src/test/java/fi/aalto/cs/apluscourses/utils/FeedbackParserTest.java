package fi.aalto.cs.apluscourses.utils;

import java.util.HashMap;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class FeedbackParserTest {

  @Test
  public void testFeedbackParser() {
    String validFeedback1 = "10 succeeded, 0 failed, 0 canceled";
    String validFeedback2 = "10 succeeded, 5 failed, 0 canceled";
    String validFeedback3 = "10 succeeded, 1 failed, 0 canceled";
    String invalidFeedback = "hello";

    Optional<HashMap<String, Integer>> results1 = FeedbackParser.testResultsMap(validFeedback1);
    Optional<HashMap<String, Integer>> results2 = FeedbackParser.testResultsMap(validFeedback2);
    Optional<HashMap<String, Integer>> results3 = FeedbackParser.testResultsMap(validFeedback3);
    Optional<HashMap<String, Integer>> results4 = FeedbackParser.testResultsMap(invalidFeedback);
    Assert.assertFalse("Invalid feedback is correct", results4.isPresent());

    Assert.assertTrue("Valid feedback 1 is correct",
            results1.isPresent()
                    && FeedbackParser.testResultString(results1.get()).equals("All tests passed"));
    Assert.assertTrue("Valid feedback 2 is correct",
            results2.isPresent()
                    && FeedbackParser.testResultString(results2.get()).equals("5 tests failed"));
    Assert.assertTrue("Valid feedback 3 is correct",
            results3.isPresent()
                    && FeedbackParser.testResultString(results3.get()).equals("1 test failed"));
  }

}
