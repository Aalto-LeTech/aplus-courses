package fi.aalto.cs.apluscourses.utils;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseHiddenElementsTest {

  @Test
  void testHidingById() {
    var hidingRules = new CourseHiddenElements(List.of(1234L), null, null);

    Assertions.assertTrue(hidingRules.shouldHideObject(1234, "blah", null));
    Assertions.assertTrue(hidingRules.shouldHideObject(1234, "blah", "en"));
    Assertions.assertFalse(hidingRules.shouldHideObject(4444, "1234", null));
  }

  @Test
  void testHidingByRegex() {
    var hidingRules = new CourseHiddenElements(null, List.of(Pattern.compile("^[a-z]{4}[A-Z]{2}$")), null);

    Assertions.assertTrue(hidingRules.shouldHideObject(1, "abcdEF", null));
    Assertions.assertFalse(hidingRules.shouldHideObject(1, "abCdEF", null));

    Assertions.assertTrue(hidingRules.shouldHideObject(1, "abcdEF", "en"));
    Assertions.assertFalse(hidingRules.shouldHideObject(1, "abcdeFGH", null));
  }

  @Test
  void testHidingByLanguageSpecificRegex() {
    var englishPatterns = List.of(
        Pattern.compile("^Exercise\\s+12"),
        Pattern.compile("course feedback survey", Pattern.CASE_INSENSITIVE)
    );

    var finnishPatterns = List.of(
        Pattern.compile("^Tehtävä\\s+12"),
        Pattern.compile("palautekyselyn tulokset", Pattern.CASE_INSENSITIVE)
    );

    var hidingRules = new CourseHiddenElements(null,
        List.of(Pattern.compile("lalala")),
        Map.of("en", englishPatterns, "fi", finnishPatterns)
    );

    Assertions.assertFalse(hidingRules.shouldHideObject(1, "Exercise   12", null));
    Assertions.assertFalse(hidingRules.shouldHideObject(1, "not Exercise 12", null));
    Assertions.assertTrue(hidingRules.shouldHideObject(1, "not lalala Exercise 12", null));

    Assertions.assertFalse(hidingRules.shouldHideObject(1, "not Exercise 12", "en"));
    Assertions.assertTrue(hidingRules.shouldHideObject(1, "Exercise 12", "en"));
    Assertions.assertFalse(hidingRules.shouldHideObject(1, "exercise 12", "en"));
    Assertions.assertTrue(hidingRules.shouldHideObject(1, "exercise 12 lalala", "en"));
    Assertions.assertTrue(hidingRules.shouldHideObject(1, "Exercise 12 (Some name here)", "en"));
    Assertions.assertTrue(hidingRules.shouldHideObject(1, "Results of course feedback survey", "en"));

    Assertions.assertTrue(hidingRules.shouldHideObject(1, "Tehtävä 12 (asdfgh)", "fi"));
    Assertions.assertFalse(hidingRules.shouldHideObject(1, "eiku Tehtävä  12", "fi"));
    Assertions.assertTrue(hidingRules.shouldHideObject(1, "Kurssipalautekyselyn tulokset", "fi"));
  }

  @Test
  void testDeserialization() {
    JSONArray testRules1 = new JSONArray("[1, 3, \"test\", {\"en\": \"en1\", \"fi\": \"fi1\"}]");
    var testObj1 = CourseHiddenElements.fromJsonObject(testRules1);

    Assertions.assertFalse(testObj1.shouldHideObject(2, "hello", null));
    Assertions.assertTrue(testObj1.shouldHideObject(3, "hello", null));
    Assertions.assertTrue(testObj1.shouldHideObject(4, "test", null));
    Assertions.assertTrue(testObj1.shouldHideObject(1, "test", null));

    Assertions.assertFalse(testObj1.shouldHideObject(2, "en1fi1", null));
    Assertions.assertTrue(testObj1.shouldHideObject(5, "hello en1", "en"));
    Assertions.assertTrue(testObj1.shouldHideObject(3, "hello en1", "fi"));
    Assertions.assertFalse(testObj1.shouldHideObject(5, "hello en1", "fi"));
    Assertions.assertFalse(testObj1.shouldHideObject(5, "hello fi", "fi"));
  }
}
