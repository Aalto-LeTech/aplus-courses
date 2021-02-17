package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ViewModelExtensions {
  private ViewModelExtensions() {

  }

  public static class TestNodeViewModel extends SelectableNodeViewModel<Object> {
    private final long id;

    /**
     * Constructor for a node view model for tests.
     */
    public TestNodeViewModel(long id, @NotNull Object model,
                             @Nullable List<SelectableNodeViewModel<?>> children) {
      super(model, children);
      this.id = id;
    }

    @Override
    public long getId() {
      return id;
    }
  }
}
