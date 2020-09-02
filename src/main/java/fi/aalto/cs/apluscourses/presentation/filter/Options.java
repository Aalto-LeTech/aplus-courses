package fi.aalto.cs.apluscourses.presentation.filter;

import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.Streamable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class Options extends AndFilter implements Streamable<Option> {
  public final Event optionsChanged = new Event();
  private final List<Option> options;

  public Options(Option... options) {
    this(Arrays.asList(options));
  }

  public Options(List<Option> options) {
    super(options);
    this.options = options;
    for (Option option : options) {
      option.isSelected.addValueObserver(this, Options::selectionChanged);
    }
  }

  protected void selectionChanged() {
    optionsChanged.trigger();
  }

  @NotNull
  @Override
  public Iterator<Option> iterator() {
    return options.iterator();
  }
}
