package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ViewModelExtensions {
  private ViewModelExtensions() {

  }

  public static class TestNodeViewModel extends SelectableNodeViewModel<Object> {
    private final long id;
    private final boolean hiddenIfNoChildren;

    /**
     * Constructor for a node view model for tests.
     */
    public TestNodeViewModel(long id, @NotNull Object model,
                             @Nullable List<SelectableNodeViewModel<?>> children,
                             boolean hiddenIfNoChildren) {
      super(model, children);
      this.id = id;
      this.hiddenIfNoChildren = hiddenIfNoChildren;
    }

    @Override
    protected void setVisibilityByFilterResult(Optional<Boolean> result) {
      if (hiddenIfNoChildren) {
        visibility = result.orElse(true)
            && this.getChildren().stream().anyMatch(SelectableNodeViewModel::isVisible);
      } else {
        visibility = result.orElse(true);
      }
    }

    @Override
    public long getId() {
      return id;
    }
  }
}
