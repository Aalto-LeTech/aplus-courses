package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.model.Exercise;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PresentationContext {
  private final Map<@NotNull String, @Nullable Object> map;

  protected PresentationContext() {
    map = new HashMap<>();
  }

  protected PresentationContext(@NotNull PresentationContext context) {
    map = new HashMap<>(context.map);
  }

  public abstract boolean presentModal(@NotNull Object viewModel);

  public abstract void showMessage(@NotNull Message message);

  public abstract void showMessageAndHide(@NotNull Message message);

  public @Nullable Object get(@NotNull String key) {
    synchronized (map) {
      return map.get(key);
    }
  }

  public void set(@NotNull String key, @Nullable Object value) {
    synchronized (map) {
      map.put(key, value);
    }
  }

  public @NotNull Object getOrSet(@NotNull String key, @NotNull Supplier<@NotNull Object> factory) {
    synchronized (map) {
      var value = map.get(key);
      if (value == null) {
        value = factory.get();
        map.put(key, value);
      }
      return value;
    }
  }

  public abstract @Nullable Exercise getSelectedExercise();
}
