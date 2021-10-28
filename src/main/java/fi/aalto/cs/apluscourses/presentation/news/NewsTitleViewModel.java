package fi.aalto.cs.apluscourses.presentation.news;

import fi.aalto.cs.apluscourses.model.News;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NewsTitleViewModel extends SelectableNodeViewModel<News> {

  private final News news;
  private final MainViewModel mainViewModel;

  protected NewsTitleViewModel(@NotNull News news, @NotNull MainViewModel mainViewModel) {
    super(news, List.of(new NewsBodyViewModel(news)));
    this.news = news;
    this.mainViewModel = mainViewModel;
  }

  @NotNull
  public String getPresentableName() {
    return news.getTitle();
  }

  @Override
  public long getId() {
    return getModel().getId();
  }

  @Override
  public void willExpand() {
    if (!news.isRead()) {
      news.setRead();
      mainViewModel.newsTreeViewModel.valueChanged();
    }
  }
}
