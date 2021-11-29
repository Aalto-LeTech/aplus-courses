package fi.aalto.cs.apluscourses.model;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;

import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import fi.aalto.cs.apluscourses.utils.parser.DefaultNewsParser;
import fi.aalto.cs.apluscourses.utils.parser.NewsParser;
import fi.aalto.cs.apluscourses.utils.parser.O1NewsParser;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class News implements Browsable {
  private final long id;

  @NotNull
  private final String url;

  @NotNull
  private final String title;

  @NotNull
  private final String[] body;

  @NotNull
  private final ZonedDateTime publish;
  private final Interfaces.ReadNews readNews;

  /**
   * Constructor.
   */
  public News(long id,
              @NotNull String url,
              @NotNull String title,
              @NotNull String[] body,
              @NotNull ZonedDateTime publish,
              @NotNull Interfaces.ReadNews readNews) {
    this.id = id;
    this.url = url;
    this.title = title;
    this.body = body;
    this.publish = publish;
    this.readNews = readNews;
  }

  /**
   * Constructs News from JSON.
   */
  public static News fromJsonObject(@NotNull JSONObject object, @NotNull Course course, @NotNull String language) {
    var id = object.getLong("id");
    var url = course.getHtmlUrl();

    var title = object.getString("title");
    var titleElement = Jsoup.parseBodyFragment(title).body();

    var body = object.getString("body");
    var bodyElement = Jsoup.parseBodyFragment(body).body();

    NewsParser parser;
    switch (course.getName()) {
      case "O1":
        parser = new O1NewsParser(language);
        break;
      default:
        parser = new DefaultNewsParser();
        break;
    }
    var titleText = parser.parseTitle(titleElement);
    var bodyText = parser.parseBody(bodyElement);

    var publishString = object.getString("publish");
    var publish = ZonedDateTime.parse(publishString);
    return new News(id, url, titleText, bodyText, publish, new Interfaces.ReadNewsImpl());
  }

  public long getId() {
    return id;
  }

  @NotNull
  public String getHtmlUrl() {
    return url;
  }

  @NotNull
  public String getTitle() {
    return title;
  }

  @NotNull
  public String[] getBody() {
    return body;
  }

  @NotNull
  public String getPublishTimeInfo() {
    return getAndReplaceText("ui.toolWindow.subTab.news.publishTime",
        publish.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
  }

  /**
   * Returns true if the news is read.
   */
  public boolean isRead() {
    var readNewsString = this.readNews.getReadNews();
    return readNewsString != null
        && Arrays.stream(readNewsString.split(";")).anyMatch(idS -> Long.parseLong(idS) == this.id);
  }

  public void setRead() {
    readNews.setNewsRead(id);
  }
}