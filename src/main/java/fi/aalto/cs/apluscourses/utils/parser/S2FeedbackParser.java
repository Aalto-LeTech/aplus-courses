package fi.aalto.cs.apluscourses.utils.parser;

import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

public class S2FeedbackParser extends FeedbackParser {

  public static final String NAME = "S2";

  /**
   * Parses TestResults for S2 from an HTML string.
   */
  @Override
  @NotNull
  public FeedbackParser.TestResults parseTestResults(@NotNull String htmlString) {
    var body = Jsoup.parseBodyFragment(htmlString).body();
    var lastP = body.select("p").last();
    if (lastP == null) {
      return new FeedbackParser.TestResults(-1, -1);
    }
    var results = lastP.text();
    var pattern = Pattern.compile("success:\\s+(\\d+),\\s+failed:\\s+(\\d+)");
    var matcher = pattern.matcher(results);
    if (!matcher.find()) {
      return new FeedbackParser.TestResults(-1, -1);
    }
    return new FeedbackParser.TestResults(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
  }
}
