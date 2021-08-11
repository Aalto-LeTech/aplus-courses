package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;

public abstract class BannerViewModel {
  public final ObservableProperty<String> text;

  public final ObservableProperty<Color> color;

  protected BannerViewModel(@NotNull ObservableProperty<String> text,
                            @NotNull Color color) {
    this.text = text;
    this.color = new ObservableReadWriteProperty<>(color);
  }
}
