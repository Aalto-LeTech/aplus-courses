package fi.aalto.cs.apluscourses.presentation.news;

import fi.aalto.cs.apluscourses.model.NewsTree;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class NewsTreeViewModel extends BaseTreeViewModel<NewsTree> {

  /**
   * Constructor.
   */
  public NewsTreeViewModel(@NotNull NewsTree newsTree, @NotNull MainViewModel mainViewModel) {
    super(newsTree,
        newsTree.getNews()
            .stream()
            .map(news -> new NewsTitleViewModel(news, mainViewModel))
            .collect(Collectors.toList()));
  }
}
