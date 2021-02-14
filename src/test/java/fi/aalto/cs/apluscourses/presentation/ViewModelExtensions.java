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
    private final boolean hideByDefault;

    /**
     * Constructor for a node view model for tests.
     */
    public TestNodeViewModel(long id, @NotNull Object model,
                             @Nullable List<SelectableNodeViewModel<?>> children,
                             boolean hideByDefault) {
      super(model, children);
      this.id = id;
      this.hideByDefault = hideByDefault;
    }

    @Override
    protected void setVisibilityByFilterResult(Optional<Boolean> result) {
      visibility = result.orElse(!hideByDefault);
    }

    @Override
    public long getId() {
      return id;
    }
  }
}
