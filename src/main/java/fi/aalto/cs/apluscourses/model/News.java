package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import java.time.ZonedDateTime;
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
  private final String body;

  @NotNull
  private final ZonedDateTime publish;
  private final Interfaces.ReadNews readNews;

  /**
   * Constructor.
   */
  public News(long id,
              @NotNull String url,
              @NotNull String title,
              @NotNull String body,
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
  public static News fromJsonObject(@NotNull JSONObject object, @NotNull Course course) {
    var id = object.getLong("id");
    var url = course.getHtmlUrl();

    var title = object.getString("title");
    var titleElement = Jsoup.parseBodyFragment(title).body();
    var titleText = titleElement.getElementsByClass("onlyfi").first().text();

    var body = object.getString("body");
    var bodyElement = Jsoup.parseBodyFragment(body).body();
    var bodyText = bodyElement.getElementsByClass("onlyfi").first().text();

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
  public String getBody() {
    return body;
  }

  @NotNull
  public ZonedDateTime getPublish() {
    return publish;
  }

  public boolean isRead() {
    return readNews.getReadNews() != null
        && Arrays.stream(readNews.getReadNews().split(";")).anyMatch(idS -> Long.parseLong(idS) == this.id);
  }

  public void setRead() {
    readNews.setNewsRead(id);
  }
}