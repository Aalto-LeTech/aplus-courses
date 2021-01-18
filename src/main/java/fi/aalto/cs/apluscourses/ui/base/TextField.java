package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.ui.utils.TwoWayBindable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
    getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent documentEvent) {
        textBindable.updateSource();
      }

      @Override
      public void removeUpdate(DocumentEvent documentEvent) {
        textBindable.updateSource();
      }

      @Override
      public void changedUpdate(DocumentEvent documentEvent) {
      }
    });
  }
}
