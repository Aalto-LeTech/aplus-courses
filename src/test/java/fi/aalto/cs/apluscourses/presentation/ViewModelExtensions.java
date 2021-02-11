package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import java.util.List;
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
    protected boolean isHiddenIfNoVisibleChildren() {
      return hiddenIfNoChildren;
    }

    @Override
    public long getId() {
      return id;
    }
  }
}
