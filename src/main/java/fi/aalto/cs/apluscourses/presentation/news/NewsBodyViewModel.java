package fi.aalto.cs.apluscourses.presentation.news;

import fi.aalto.cs.apluscourses.model.news.NewsItem;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import org.jetbrains.annotations.NotNull;

public class NewsBodyViewModel extends SelectableNodeViewModel<NewsItem> {

  private final NewsItem news;

  protected NewsBodyViewModel(@NotNull NewsItem news) {
    super(news, null);
    this.news = news;
  }

  @NotNull
  public String getPresentableName() {
    return news.getBody();
  }

  @Override
  public long getId() {
    return getModel().getId();
  }
}
