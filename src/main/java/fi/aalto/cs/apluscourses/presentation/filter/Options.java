package fi.aalto.cs.apluscourses.presentation.filter;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.Streamable;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class Options extends AndFilter implements Streamable<Option> {
  public final Event optionsChanged = new Event();
  private final List<Option> optionList;

  public Options(Option... options) {
    this(List.of(options));
  }

  protected Options(List<Option> optionList) {
    super(optionList);
    this.optionList = optionList;
    for (Option option : optionList) {
      option.isSelected.addSimpleObserver(this, Options::selectionChanged);
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

  public void toggleAll() {
    boolean isAnyActive = isAnyActive();
    optionList.forEach(option -> option.isSelected.set(isAnyActive));
  }

  /**
   * If any filter is active, returns true.
   */
  public boolean isAnyActive() {
    return optionList.stream()
            .anyMatch(option -> {
              var selected = option.isSelected.get();
              return selected != null && !selected;
            });
  }

  /**
   * The text description for toggling all options.
   */
  public String getSelectText() {
    return this.isAnyActive()
            ? getText("presentation.filter.selectAll")
            : getText("presentation.filter.deselectAll");
  }
}
