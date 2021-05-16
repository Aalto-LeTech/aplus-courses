package fi.aalto.cs.apluscourses;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;

public abstract class BannerViewModel {
  public final ObservableProperty<String> text = new ObservableReadWriteProperty<>("");

  public abstract void update();
}
