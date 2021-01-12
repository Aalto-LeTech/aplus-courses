package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.ui.utils.TwoWayBindable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;

/**
 * A class deriving from {@link JTextField} that works with the {@link TwoWayBindable} class to
 * reflect its text input to the source property.
 */
public class TextField extends JTextField {
  public final transient TwoWayBindable<TextField, String> textBindable =
      new TwoWayBindable<>(this, TextField::setText, TextField::getText);

  /**
   * Construct a TextField.
   */
  public TextField() {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        textBindable.updateSource();
      }
    });
  }
}
