package fi.aalto.cs.apluscourses.presentation.news;

import fi.aalto.cs.apluscourses.model.News;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import org.jetbrains.annotations.NotNull;

public class NewsBodyViewModel extends SelectableNodeViewModel<News> {

  private final News news;

  protected NewsBodyViewModel(@NotNull News news) {
    super(news, null);
    this.news = news;
  }

  @NotNull
  public String[] getPresentableName() {
    return news.getBody();
  }

  @Override
  public long getId() {
    return getModel().getId();
  }
}
