package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.ui.utils.TwoWayBindable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A class deriving from {@link JTextField} that works with the {@link TwoWayBindable} class to
 * reflect its text input to the source property.
 */
public class TextField extends JTextField {
  private static final Logger logger = LoggerFactory.getLogger(TextField.class);

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
        try {
          throw new OperationNotSupportedException();
        } catch (OperationNotSupportedException e) {
          logger.info("Operation not supported", e);
        }
      }
    });
  }
}
