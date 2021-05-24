package fi.aalto.cs.apluscourses;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;

public abstract class BannerViewModel {
  public final ObservableProperty<String> text = new ObservableReadWriteProperty<>("");

  public final ObservableProperty<Color> color;

  protected BannerViewModel(@NotNull Color color) {
    this.color = new ObservableReadWriteProperty<>(color);
  }

  public abstract void update();
}
