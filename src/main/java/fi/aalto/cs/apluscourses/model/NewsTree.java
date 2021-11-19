package fi.aalto.cs.apluscourses.model;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NewsTree {
  @NotNull
  private final List<News> news;

  public NewsTree() {
    this(Collections.emptyList());
  }

  public NewsTree(@NotNull List<News> news) {
    this.news = news;
  }

  @NotNull
  public List<News> getNews() {
    return news;
  }

  public void setAllRead() {
    news.forEach(News::setRead);
  }

  public long unreadCount() {
    return news.stream().filter(n -> !n.isRead()).count();
  }

}
