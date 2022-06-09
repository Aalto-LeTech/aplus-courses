package fi.aalto.cs.apluscourses.utils.parser;

import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

public class O1FeedbackParser extends FeedbackParser {

  public static final String NAME = "O1";

  /**
   * Parses TestResults for O1 from an HTML string.
   */
  @Override
  @NotNull
  public FeedbackParser.TestResults parseTestResults(@NotNull String htmlString) {
    var body = Jsoup.parseBodyFragment(htmlString).body();
    var lastH3 = body.getElementsByTag("h3").last();
    if (lastH3 == null || lastH3.nextElementSibling() == null) {
      return new FeedbackParser.TestResults(-1, -1);
    }
    var results = lastH3.nextElementSibling().text();
    var pattern = Pattern.compile("(\\d+)\\s+succeeded,\\s+(\\d+)\\s+failed,\\s+(\\d+)\\s+canceled");
    var matcher = pattern.matcher(results);
    if (!matcher.find()) {
      return new FeedbackParser.TestResults(-1, -1);
    }
    return new FeedbackParser.TestResults(Integer.parseInt(matcher.group(1)),
        Integer.parseInt(matcher.group(2)) + Integer.parseInt(matcher.group(3)));
  }
}
