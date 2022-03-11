package fi.aalto.cs.apluscourses.utils.parser;

import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

public class FeedbackParser {
  private FeedbackParser() {
  }

  /**
   * Parses TestResults for O1 from an HTML string.
   */
  public static TestResults parseO1TestResults(@NotNull String htmlString) {
    var body = Jsoup.parseBodyFragment(htmlString).body();
    var lastH3 = body.getElementsByTag("h3").last();
    if (lastH3 == null || lastH3.nextElementSibling() == null) {
      return new TestResults(-1, -1);
    }
    var results = lastH3.nextElementSibling().text();
    var pattern = Pattern.compile("(\\d+)\\s+succeeded,\\s+(\\d+)\\s+failed,\\s+(\\d+)\\s+canceled");
    var matcher = pattern.matcher(results);
    if (!matcher.find()) {
      return new TestResults(-1, -1);
    }
    return new TestResults(Integer.parseInt(matcher.group(1)),
        Integer.parseInt(matcher.group(2)) + Integer.parseInt(matcher.group(3)));
  }

  /**
   * Parses TestResults for S2 from an HTML string.
   */
  public static TestResults parseS2TestResults(@NotNull String htmlString) {
    var body = Jsoup.parseBodyFragment(htmlString).body();
    var lastP = body.select("p").last();
    if (lastP == null) {
      return new TestResults(-1, -1);
    }
    var results = lastP.text();
    var pattern = Pattern.compile("success:\\s+(\\d+),\\s+failed:\\s+(\\d+)");
    var matcher = pattern.matcher(results);
    if (!matcher.find()) {
      return new TestResults(-1, -1);
    }
    return new TestResults(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
  }

  public static class TestResults {
    public final int succeeded;
    public final int failed;

    public TestResults(int succeeded, int failed) {
      this.succeeded = succeeded;
      this.failed = failed;
    }
  }
}
