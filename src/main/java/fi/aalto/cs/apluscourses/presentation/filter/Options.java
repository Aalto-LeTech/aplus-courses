package fi.aalto.cs.apluscourses.presentation.filter;

import fi.aalto.cs.apluscourses.utils.Event;
import java.util.Arrays;
import java.util.List;

public class Options extends AndFilter {
  public final Event optionsChanged = new Event();

  public Options(Option... options) {
    this(Arrays.asList(options));
  }

  public Options(List<Option> options) {
    super(options);
    for (Option option : options) {
      option.isSelected.addValueObserver(this, Options::selectionChanged);
    }
  }

  protected void selectionChanged() {
    optionsChanged.trigger();
  }
}
