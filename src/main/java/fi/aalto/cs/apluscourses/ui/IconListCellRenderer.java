package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.Function;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Custom renderer for items stored in {@link javax.swing.JComboBox}.
 */
public class IconListCellRenderer<T> extends SimpleListCellRenderer<T> {

  private static final long serialVersionUID = 1136883770955918127L;

  @Nullable
  private final String nullText;
  @Nullable
  private final Icon icon;
  @NotNull
  private final Function<? super T, String> getText;

  public IconListCellRenderer(@Nullable Icon icon) {
    this(null, Objects::toString, icon);
  }

  public IconListCellRenderer(@NotNull Function<? super T, String> getText, @Nullable Icon icon) {
    this(null, getText, icon);
  }

  /**
   * A complete constructor.
   *
   * @param nullText Test that is shown when no item is selected.
   * @param getText Reference to a function/method.
   * @param icon Icon to be set to items.
   */
  public IconListCellRenderer(@Nullable String nullText,
                              @NotNull Function<? super T, String> getText,
                              @Nullable Icon icon) {
    this.nullText = nullText;
    this.getText = getText;
    this.icon = icon;
  }

  @Override
  public void customize(@NotNull JList<? extends T> list,
                        T value,
                        int index,
                        boolean selected,
                        boolean hasFocus) {
    if (value == null) {
      setText(nullText);
      setIcon(null);
    } else {
      setText(getText.fun(value));
      setIcon(icon);
    }
  }
}
