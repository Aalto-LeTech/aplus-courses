package fi.aalto.cs.apluscourses.presentation.dialogs;

import fi.aalto.cs.apluscourses.presentation.dialogs.Dialog;
import fi.aalto.cs.apluscourses.presentation.dialogs.Dialogs;
import fi.aalto.cs.apluscourses.utils.FactorySelector;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestDialogs extends FactorySelector<Void, Dialog> implements Dialogs {

  @NotNull
  @Override
  public Dialog create(@NotNull Object object) {
    return create(object, null);
  }

  public static class FactoryImpl<T> implements FactorySelector.Factory<T, Void, Dialog> {

    private final Predicate<T> showAndGetFunc;

    public FactoryImpl(Predicate<T> showAndGetFunc) {
      this.showAndGetFunc = showAndGetFunc;
    }

    @NotNull
    public Dialog create(@NotNull T viewModel, @Nullable Void none) {
      return () -> showAndGetFunc.test(viewModel);
    }
  }
}
