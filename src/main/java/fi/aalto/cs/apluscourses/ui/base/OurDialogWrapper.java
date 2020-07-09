package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public abstract class OurDialogWrapper extends DialogWrapper {
  private final List<ValidationItem> validationItems = new ArrayList<>();

  protected OurDialogWrapper(@Nullable Project project) {
    super(project);
  }

  /**
   * Registers a validation item that will be checked when the dialog is validated.
   *
   * @param validationItem A validation item to be registered.
   */
  public void registerValidationItem(ValidationItem validationItem) {
    synchronized (validationItems) {
      validationItems.add(validationItem);
    }
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    synchronized (validationItems) {
      for (ValidationItem validationItem : validationItems) {
        String validationError = validationItem.validate();
        if (validationError != null) {
          return new ValidationInfo(validationError, validationItem.getComponent());
        }
      }
      return null;
    }
  }

  public interface ValidationItem {
    @Nullable
    String validate();

    @Nullable
    default JComponent getComponent() {
      return null;
    }
  }
}
