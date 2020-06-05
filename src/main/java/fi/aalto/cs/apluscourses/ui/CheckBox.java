package fi.aalto.cs.apluscourses.ui;

import fi.aalto.cs.apluscourses.utils.ObservableProperty;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;
import org.jetbrains.annotations.NotNull;

/**
 * A class deriving from {@link JCheckBox} that works with the {@link ObservableProperty} class. It
 * takes two observable properties, {@code isChecked} and {@code isEnabled}. It adds observers to
 * both that change the selected and enabled status of the check box when the observable properties
 * change. It also sets the value of {@code isChecked} to {@code false} or {@true} if the check box
 * is unchecked or checked.
 */
public class CheckBox extends JCheckBox {

  // ObservableProperty only has weak references to its observers, so we have them as instance
  // variables.
  private transient ObservableProperty.ValueObserver<Boolean> isCheckedObserver
      = super::setSelected;
  private transient ObservableProperty.ValueObserver<Boolean> isEnabledObserver
      = super::setEnabled;

  /**
   * Construct a check box with the given observable properties.
   */
  public CheckBox(@NotNull String text,
                  @NotNull ObservableProperty<Boolean> isChecked,
                  @NotNull ObservableProperty<Boolean> isEnabled) {
    super(text);

    isChecked.addValueObserver(isCheckedObserver);
    isEnabled.addValueObserver(isEnabledObserver);

    addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED && Boolean.FALSE.equals(isChecked.get())) {
        isChecked.set(true);
      } else if (e.getStateChange() == ItemEvent.DESELECTED
          && Boolean.TRUE.equals(isChecked.get())) {
        isChecked.set(false);
      }
    });
  }

}
