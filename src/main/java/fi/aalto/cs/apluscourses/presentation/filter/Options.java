package fi.aalto.cs.apluscourses.presentation.filter;

import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.Streamable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Options extends AndFilter implements Streamable<Option> {
  public final Event optionsChanged = new Event();
  private final List<Option> optionList;

  public Options(Option... options) {
    this(Arrays.asList(options));
  }

  protected Options(List<Option> optionList) {
    super(optionList);
    this.optionList = optionList;
    for (Option option : optionList) {
      option.isSelected.addValueObserver(this, Options::selectionChanged);
    }
  }

  protected void selectionChanged() {
    optionsChanged.trigger();
  }

  @NotNull
  @Override
  public Iterator<Option> iterator() {
    return optionList.iterator();
  }
}
